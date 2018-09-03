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
                        String commandLine = getCommandLine(project, sourceSettings);
                        try {
                            PreprocessorRunner runner = new PreprocessorRunner(commandLine, compilerWorkingDir, sourceText, indicator);
                            String preprocessedText = runner.getStdout();
                            if (runner.getExitCode() == 0 && !preprocessedText.isEmpty()) {
                                ApplicationManager.getApplication().invokeLater(() -> preprocessedSourceConsumer.setPreprocessedSource(new PreprocessedSource(sourceSettings, preprocessedText)));
                            } else {
                                ApplicationManager.getApplication().invokeLater(() -> preprocessedSourceConsumer.clearPreprocessedSource("Command:\n" + commandLine + "\nWorking directory:\n" + compilerWorkingDir.getAbsolutePath() + "\nExit code " + runner.getExitCode() + "\nOutput:\n" + preprocessedText + "Errors:\n" + runner.getStderr()));
                            }
                        } catch (ProcessCanceledException canceledException) {
                            // empty
                        } catch (Exception exception) {
                            ApplicationManager.getApplication().invokeLater(() -> preprocessedSourceConsumer.clearPreprocessedSource("Command:\n" + commandLine + "\nException: " + exception.getMessage()));
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
    private static String getCommandLine(@NotNull Project project, @NotNull SourceSettings sourceSettings) {
        return "\"" + sourceSettings.getCompiler().getAbsolutePath()
             + "\" " + sourceSettings.getSwitches().stream().map(s -> "\"" + s + "\"").collect(Collectors.joining(" "))
             + " \"-I" + project.getBasePath() + "\""
             + " -E"
             + " -o -"
             + " -x " + sourceSettings.getLanguage().getDisplayName().toLowerCase()
             + " -c -";
    }

    private static boolean isSupportedCompilerType(@NotNull OCCompilerKind compilerKind) {
        return compilerKind == GCC || compilerKind == CLANG;
    }
}
