package com.compilerexplorer.preprocessor;

import com.compilerexplorer.common.PreprocessedSource;
import com.compilerexplorer.common.PreprocessedSourceConsumer;
import com.compilerexplorer.common.SourceSettings;
import com.compilerexplorer.common.SourceSettingsConsumer;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator;
import com.intellij.openapi.project.Project;
import com.jetbrains.cidr.lang.workspace.compiler.OCCompilerKind;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.stream.Collectors;

import static com.jetbrains.cidr.lang.workspace.compiler.OCCompilerKind.*;

public class SourcePreprocessor implements SourceSettingsConsumer {
    @NotNull
    private final Project project;
    @NotNull
    private final PreprocessedSourceConsumer preprocessedSourceConsumer;
    @Nullable
    private BackgroundableProcessIndicator currentProgressIndicator;

    public SourcePreprocessor(@NotNull Project project_, @NotNull PreprocessedSourceConsumer preprocessedSourceConsumer_) {
        project = project_;
        preprocessedSourceConsumer = preprocessedSourceConsumer_;
    }

    @Override
    public void setSourceSetting(@NotNull SourceSettings sourceSettings) {
        if (isSupportedCompilerType(sourceSettings.getCompilerKind())) {
            Document document = FileDocumentManager.getInstance().getDocument(sourceSettings.getSource());
            if (document != null) {
                String sourceText = document.getText();
                File compiler = sourceSettings.getCompiler();
                File compilerWorkingDir = compiler.getParentFile();

                if (currentProgressIndicator != null) {
                    currentProgressIndicator.cancel();
                }

                Task.Backgroundable task = new Task.Backgroundable(project, "Preprocessing " + sourceSettings.getSource().getPresentableName()) {
                    @Override
                    public void run(@NotNull ProgressIndicator indicator) {
                        String preprocessorCommandLine = getPreprocessorCommandLine(project, sourceSettings);
                        try {
                            PreprocessorRunner preprocessorRunner = new PreprocessorRunner(preprocessorCommandLine, compilerWorkingDir, sourceText, indicator);
                            String preprocessedText = preprocessorRunner.getStdout();
                            if (preprocessorRunner.getExitCode() == 0 && !preprocessedText.isEmpty()) {
                                String versionCommandLine = getVersionCommandLine(sourceSettings);
                                try {
                                    PreprocessorRunner versionRunner = new PreprocessorRunner(versionCommandLine, compilerWorkingDir, "", indicator);
                                    String versionText = versionRunner.getStderr();
                                    if (versionRunner.getExitCode() == 0 && !versionText.isEmpty()) {
                                        String compilerVersion = parseCompilerVersion(sourceSettings.getCompilerKind(), versionText);
                                        String compilerTarget = parseCompilerTarget(versionText);
                                        if (!compilerVersion.isEmpty() && !compilerTarget.isEmpty()) {
                                            ApplicationManager.getApplication().invokeLater(() -> preprocessedSourceConsumer.setPreprocessedSource(new PreprocessedSource(sourceSettings, preprocessedText, sourceSettings.getLanguage().getDisplayName().toLowerCase(), sourceSettings.getCompilerKind().toString().toLowerCase(), compilerVersion, compilerTarget)));
                                        } else {
                                            ApplicationManager.getApplication().invokeLater(() -> preprocessedSourceConsumer.clearPreprocessedSource("Cannot parse compiler version:\n" + versionText));
                                        }
                                    } else {
                                        ApplicationManager.getApplication().invokeLater(() -> preprocessedSourceConsumer.clearPreprocessedSource("Command:\n" + versionCommandLine + "\nWorking directory:\n" + compilerWorkingDir.getAbsolutePath() + "\nExit code " + versionRunner.getExitCode() + "\nOutput:\n" + versionRunner.getStdout() + "Errors:\n" + versionText));
                                    }
                                } catch (ProcessCanceledException canceledException) {
                                    // empty
                                } catch (Exception exception) {
                                    ApplicationManager.getApplication().invokeLater(() -> preprocessedSourceConsumer.clearPreprocessedSource("Command:\n" + versionCommandLine + "\nException: " + exception.getMessage()));
                                }
                            } else {
                                ApplicationManager.getApplication().invokeLater(() -> preprocessedSourceConsumer.clearPreprocessedSource("Command:\n" + preprocessorCommandLine + "\nWorking directory:\n" + compilerWorkingDir.getAbsolutePath() + "\nExit code " + preprocessorRunner.getExitCode() + "\nOutput:\n" + preprocessedText + "Errors:\n" + preprocessorRunner.getStderr()));
                            }
                        } catch (ProcessCanceledException canceledException) {
                            // empty
                        } catch (Exception exception) {
                            ApplicationManager.getApplication().invokeLater(() -> preprocessedSourceConsumer.clearPreprocessedSource("Command:\n" + preprocessorCommandLine + "\nException: " + exception.getMessage()));
                        }
                    }
                };
                currentProgressIndicator = new BackgroundableProcessIndicator(task);
                ProgressManager.getInstance().runProcessWithProgressAsynchronously(task, currentProgressIndicator);
            } else {
                preprocessedSourceConsumer.clearPreprocessedSource("Cannot get document " + sourceSettings.getSource().getPath());
            }
        } else {
            preprocessedSourceConsumer.clearPreprocessedSource("Unsupported compiler type \"" + sourceSettings.getCompilerKind().toString() + "\" for " + sourceSettings.getSource().getPath());
        }
    }

    @Override
    public void clearSourceSetting(@NotNull String reason) {
        preprocessedSourceConsumer.clearPreprocessedSource(reason);
    }

    @NotNull
    private static String getPreprocessorCommandLine(@NotNull Project project, @NotNull SourceSettings sourceSettings) {
        return "\"" + sourceSettings.getCompiler().getAbsolutePath() + "\""
             + " " + sourceSettings.getSwitches().stream().map(s -> "\"" + s + "\"").collect(Collectors.joining(" "))
             + " \"-I" + project.getBasePath() + "\""
             + " -E"
             + " -o -"
             + " -x " + sourceSettings.getLanguage().getDisplayName().toLowerCase()
             + " -c -";
    }

    @NotNull
    private static String getVersionCommandLine(@NotNull SourceSettings sourceSettings) {
        return "\"" + sourceSettings.getCompiler().getAbsolutePath() + "\""
                + " -v";
    }

    private static boolean isSupportedCompilerType(@NotNull OCCompilerKind compilerKind) {
        return compilerKind == GCC || compilerKind == CLANG;
    }

    @NotNull
    private static String parseCompilerVersion(@NotNull OCCompilerKind compilerKind, @NotNull String versionText) {
        return versionText.replace('\n', ' ').replaceAll(".*" + compilerKind.toString().toLowerCase() + " version ([^ ]*).*", "$1");
    }

    @NotNull
    private static String parseCompilerTarget(@NotNull String versionText) {
        return versionText.replace('\n', ' ').replaceAll(".*Target: ([^-]*).*", "$1");
    }
}
