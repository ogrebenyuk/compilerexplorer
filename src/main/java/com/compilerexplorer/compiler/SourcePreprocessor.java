package com.compilerexplorer.compiler;

import com.compilerexplorer.common.*;
import com.compilerexplorer.common.datamodel.*;
import com.compilerexplorer.common.datamodel.state.SettingsState;
import com.compilerexplorer.compiler.common.CompilerRunner;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.jetbrains.cidr.lang.workspace.compiler.GCCCompiler;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.lang.Error;
import java.util.Arrays;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class SourcePreprocessor implements Consumer<SourceRemoteMatched> {
    @NotNull
    private final Project project;
    @NotNull
    private final Consumer<PreprocessedSource> preprocessedSourceConsumer;
    @NotNull
    private final Consumer<Error> errorConsumer;
    @NotNull
    private final TaskRunner taskRunner;
    @Nullable
    private SourceRemoteMatched lastPreprocessableSource;

    public SourcePreprocessor(@NotNull Project project_,
                              @NotNull Consumer<PreprocessedSource> preprocessedSourceConsumer_,
                              @NotNull Consumer<Error> errorConsumer_,
                              @NotNull TaskRunner taskRunner_) {
        project = project_;
        preprocessedSourceConsumer = preprocessedSourceConsumer_;
        errorConsumer = errorConsumer_;
        taskRunner = taskRunner_;
    }

    @Override
    public void accept(@NotNull SourceRemoteMatched preprocessableSource) {
        lastPreprocessableSource = preprocessableSource;
        SettingsState state = SettingsProvider.getInstance(project).getState();

        if (!state.getEnabled()) {
            return;
        }

        SourceSettings sourceSettings = preprocessableSource.getSourceCompilerSettings().getSourceSettings();
        Document document = FileDocumentManager.getInstance().getDocument(sourceSettings.getSource());
        if (document == null) {
            errorLater("Cannot get document " + sourceSettings.getSourcePath());
            return;
        }

        String sourceText = "# 1 \"" + sourceSettings.getSourcePath().replaceAll("\\\\", "\\\\\\\\") + "\"\n" + document.getText();
        if (!state.getPreprocessLocally()) {
            preprocessedSourceConsumer.accept(new PreprocessedSource(preprocessableSource, sourceText));
            return;
        }

        String name = sourceSettings.getSourceName();
        File compiler = preprocessableSource.getSourceCompilerSettings().getSourceSettings().getCompiler();
        File compilerWorkingDir = compiler.getParentFile();
        taskRunner.runTask(new Task.Backgroundable(project, "Preprocessing " + name) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                String[] preprocessorCommandLine = getPreprocessorCommandLine(project, sourceSettings, state.getAdditionalSwitches());
                try {
                    CompilerRunner compilerRunner = new CompilerRunner(preprocessorCommandLine, compilerWorkingDir, sourceText, indicator);
                    String preprocessedText = compilerRunner.getStdout();
                    if (compilerRunner.getExitCode() == 0 && !preprocessedText.isEmpty()) {
                        ApplicationManager.getApplication().invokeLater(() -> preprocessedSourceConsumer.accept(new PreprocessedSource(preprocessableSource, preprocessedText)));
                    } else {
                        errorLater("Cannot run preprocessor:\n" + String.join(" ", preprocessorCommandLine) + "\nWorking directory:\n" + compilerWorkingDir.getAbsolutePath() + "\nExit code " + compilerRunner.getExitCode() + "\nOutput:\n" + preprocessedText + "Errors:\n" + compilerRunner.getStderr());
                    }
                } catch (ProcessCanceledException canceledException) {
                    //errorLater("Canceled preprocessing " + name + ":\n" + String.join(" ", preprocessorCommandLine));
                } catch (Exception exception) {
                    errorLater("Cannot preprocess " + name + ":\n" + String.join(" ", preprocessorCommandLine) + "\nException: " + exception.getMessage());
                }
            }
        });
    }

    @NotNull
    private static String[] getPreprocessorCommandLine(@NotNull Project project, @NotNull SourceSettings sourceSettings, @NotNull String additionalSwitches) {
        return Stream.concat(
                Stream.concat(
                        Stream.concat(
                                Stream.of(sourceSettings.getCompiler().getAbsolutePath()),
                                sourceSettings.getSwitches().stream()
                        ),
                        Arrays.stream(additionalSwitches.split(" "))
                ),
                Stream.of(
                        "-I" + project.getBasePath(),
                        "-E",
                        "-o", "-",
                        sourceSettings.getLanguageSwitch(),
                        "-c", "-"
                )
        ).toArray(String[]::new);
    }

    private void errorLater(@NotNull String text) {
        ApplicationManager.getApplication().invokeLater(() -> errorConsumer.accept(new Error(text)));
    }

    public void refresh() {
        if (lastPreprocessableSource != null && SettingsProvider.getInstance(project).getState().getEnabled()) {
            accept(lastPreprocessableSource);
        }
    }
}
