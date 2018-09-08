package com.compilerexplorer.compiler;

import com.compilerexplorer.common.*;
import com.compilerexplorer.common.datamodel.*;
import com.compilerexplorer.common.datamodel.state.SettingsState;
import com.compilerexplorer.common.datamodel.state.StateConsumer;
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

public class SourcePreprocessor implements PreprocessableSourceConsumer, StateConsumer {
    @NotNull
    private final Project project;
    @NotNull
    private final PreprocessedSourceConsumer preprocessedSourceConsumer;
    @Nullable
    private BackgroundableProcessIndicator currentProgressIndicator;
    @Nullable
    private PreprocessableSource preprocessableSource;
    @Nullable
    private String reason;
    private boolean preprocessLocally = SettingsState.DEFAULT_PREPROCESS_LOCALLY;
    private boolean useRemoteDefines = SettingsState.DEFAULT_PREPROCESS_LOCALLY && SettingsState.DEFAULT_USE_REMOTE_DEFINES;

    public SourcePreprocessor(@NotNull Project project_, @NotNull PreprocessedSourceConsumer preprocessedSourceConsumer_) {
        project = project_;
        preprocessedSourceConsumer = preprocessedSourceConsumer_;
    }

    @Override
    public void setPreprocessableSource(@NotNull PreprocessableSource preprocessableSource_) {
        if (preprocessableSource == null || !preprocessableSource.equals(preprocessableSource_)) {
            preprocessableSource = preprocessableSource_;
            reason = null;
            refresh();
        }
    }

    @Override
    public void clearPreprocessableSource(@NotNull String reason_) {
        if (reason == null || !reason.equals(reason_)) {
            preprocessableSource = null;
            reason = reason_;
            refresh();
        }
    }

    @Override
    public void stateChanged() {
        SettingsState state = SettingsProvider.getInstance(project).getState();
        boolean newPreprocessLocally = state.getPreprocessLocally();
        boolean newUseRemoteDefines = state.getPreprocessLocally() && state.getUseRemoteDefines();
        boolean changed = newPreprocessLocally != preprocessLocally || newUseRemoteDefines != useRemoteDefines;
        if (changed) {
            preprocessLocally = newPreprocessLocally;
            useRemoteDefines = newUseRemoteDefines;
            refresh();
        }
    }

    private void refresh() {
        if (reason != null) {
            preprocessedSourceConsumer.clearPreprocessedSource(reason);
            return;
        }

        if (preprocessableSource == null) {
            preprocessedSourceConsumer.clearPreprocessedSource("No preprocessable source");
            return;
        }

        SourceSettings sourceSettings = preprocessableSource.getSourceRemoteMatched().getSourceCompilerSettings().getSourceSettings();
        VirtualFile source = sourceSettings.getSource();
        Document document = FileDocumentManager.getInstance().getDocument(source);
        if (document == null) {
            preprocessedSourceConsumer.clearPreprocessedSource("Cannot get document " + source.getPath());
            return;
        }

        String sourceText = useRemoteDefines ? preprocessableSource.getDefines().getDefines() + document.getText() : document.getText();
        if (!preprocessLocally) {
            preprocessedSourceConsumer.setPreprocessedSource(new PreprocessedSource(preprocessableSource, sourceText));
            return;
        }

        String name = source.getPresentableName();
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
                    ApplicationManager.getApplication().invokeLater(() -> preprocessedSourceConsumer.clearPreprocessedSource("Canceled preprocessing " + name));
                } catch (Exception exception) {
                    ApplicationManager.getApplication().invokeLater(() -> preprocessedSourceConsumer.clearPreprocessedSource("Cannot preprocess " + name + ":\n" + String.join(" ", preprocessorCommandLine) + "\nException: " + exception.getMessage()));
                }
            }
        };
        currentProgressIndicator = new BackgroundableProcessIndicator(task);
        ProgressManager.getInstance().runProcessWithProgressAsynchronously(task, currentProgressIndicator);
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
