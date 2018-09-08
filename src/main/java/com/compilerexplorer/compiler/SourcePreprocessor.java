package com.compilerexplorer.compiler;

import com.compilerexplorer.common.*;
import com.compilerexplorer.compiler.common.CompilerRunner;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.stream.Stream;

public class SourcePreprocessor implements PreprocessableSourceConsumer {
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
    public void setPreprocessableSource(@NotNull PreprocessableSource preprocessableSource) {
        SourceSettings sourceSettings = preprocessableSource.getSourceRemoteMatched().getSourceCompilerSettings().getSourceSettings();
        VirtualFile source = sourceSettings.getSource();
        Document document = FileDocumentManager.getInstance().getDocument(source);
        if (document == null) {
            preprocessedSourceConsumer.clearPreprocessedSource("Cannot get document " + source.getPath());
            return;
        }

        String name = source.getPresentableName();
        String sourceText = preprocessableSource.getDefines().getDefines() + document.getText();
        File compiler = preprocessableSource.getSourceRemoteMatched().getSourceCompilerSettings().getSourceSettings().getCompiler();
        File compilerWorkingDir = compiler.getParentFile();
        if (currentProgressIndicator != null) {
            currentProgressIndicator.cancel();
        }
        preprocessedSourceConsumer.clearPreprocessedSource("Preprocessing " + name + "...");
        Task.Backgroundable task = new Task.Backgroundable(project, "Preprocessing " + name) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                String[] preprocessorCommandLine = getPreprocessorCommandLine(project, sourceSettings);
                try {
                    CompilerRunner compilerRunner = new CompilerRunner(preprocessorCommandLine, compilerWorkingDir, sourceText, indicator);
                    String preprocessedText = compilerRunner.getStdout();
                    if (compilerRunner.getExitCode() == 0 && !preprocessedText.isEmpty()) {
                        ApplicationManager.getApplication().invokeLater(() -> preprocessedSourceConsumer.setPreprocessedSource(new PreprocessedSource(preprocessableSource, preprocessedText)));
                    } else {
                        ApplicationManager.getApplication().invokeLater(() -> preprocessedSourceConsumer.clearPreprocessedSource("Cannot run preprocessor:\n" + String.join(" ", preprocessorCommandLine) + "\nWorking directory:\n" + compilerWorkingDir.getAbsolutePath() + "\nExit code " + compilerRunner.getExitCode() + "\nOutput:\n" + preprocessedText + "Errors:\n" + compilerRunner.getStderr()));
                    }
                } catch (ProcessCanceledException canceledException) {
                    // empty
                } catch (Exception exception) {
                    ApplicationManager.getApplication().invokeLater(() -> preprocessedSourceConsumer.clearPreprocessedSource("Cannot preprocess " + name + ":\n" + String.join(" ", preprocessorCommandLine) + "\nException: " + exception.getMessage()));
                }
            }
        };
        currentProgressIndicator = new BackgroundableProcessIndicator(task);
        ProgressManager.getInstance().runProcessWithProgressAsynchronously(task, currentProgressIndicator);
    }

    @Override
    public void clearPreprocessableSource(@NotNull String reason) {
        preprocessedSourceConsumer.clearPreprocessedSource(reason);
    }

    @NotNull
    private static String[] getPreprocessorCommandLine(@NotNull Project project, @NotNull SourceSettings sourceSettings) {
        return Stream.concat(
                Stream.concat(
                        Stream.of(sourceSettings.getCompiler().getAbsolutePath()),
                        sourceSettings.getSwitches().stream()),
                Stream.of(
                        "-I" + project.getBasePath(),
                        "-undef",
                        "-E",
                        "-o", "-",
                        "-x", sourceSettings.getLanguage().getDisplayName().toLowerCase(),
                        "-c", "-")
        ).toArray(String[]::new);
    }
}
