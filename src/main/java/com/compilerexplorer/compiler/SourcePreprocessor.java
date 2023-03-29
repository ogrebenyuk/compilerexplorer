package com.compilerexplorer.compiler;

import com.compilerexplorer.common.*;
import com.compilerexplorer.datamodel.*;
import com.compilerexplorer.datamodel.state.SettingsState;
import com.compilerexplorer.compiler.common.CompilerRunner;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class SourcePreprocessor extends RefreshableComponent<SourceCompilerSettings> {
    @NotNull
    private final Project project;
    @NotNull
    private final Consumer<PreprocessedSource> preprocessedSourceConsumer;
    @NotNull
    private final TaskRunner taskRunner;

    public SourcePreprocessor(@NotNull Project project_,
                              @NotNull Consumer<PreprocessedSource> preprocessedSourceConsumer_,
                              @NotNull TaskRunner taskRunner_) {
        project = project_;
        preprocessedSourceConsumer = preprocessedSourceConsumer_;
        taskRunner = taskRunner_;
    }

    @Override
    public void accept(@NotNull SourceCompilerSettings sourceCompilerSettings) {
        super.accept(sourceCompilerSettings);

        SettingsState state = CompilerExplorerSettingsProvider.getInstance(project).getState();

        if (!state.getEnabled()) {
            return;
        }

        SourceSettings sourceSettings = sourceCompilerSettings.sourceSettingsConnected.sourceSettings.selectedSourceSettings;
        @Nullable Document document = sourceSettings != null ? FileDocumentManager.getInstance().getDocument(sourceSettings.source) : null;
        taskRunner.runTask(new Task.Backgroundable(project, "Preprocessing " + (sourceSettings != null ? sourceSettings.sourceName : null)) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                PreprocessedSource preprocessedSource = new PreprocessedSource(sourceCompilerSettings);
                if (sourceCompilerSettings.isValid()) {
                    if (document != null) {
                        String sourceText = "# 1 \"" + sourceSettings.sourcePath.replaceAll("\\\\", "\\\\\\\\") + "\"\n" + document.getText();
                        preprocessedSource.preprocessLocally = state.getPreprocessLocally();
                        if (state.getPreprocessLocally()) {
                            String[] preprocessorCommandLine = getPreprocessorCommandLine(project, sourceSettings, state.getAdditionalSwitches(), state.getIgnoreSwitches());
                            preprocessedSource.preprocessorCommandLine = preprocessorCommandLine;
                            try {
                                File compilerWorkingDir = sourceSettings.compilerWorkingDir;
                                preprocessedSource.compilerWorkingDir = compilerWorkingDir;
                                CompilerRunner compilerRunner = new CompilerRunner(sourceSettings.host, preprocessorCommandLine, compilerWorkingDir, sourceText, indicator, state.getCompilerTimeoutMillis());
                                preprocessedSource.preprocessorExitCode = compilerRunner.getExitCode();
                                preprocessedSource.preprocessedText = compilerRunner.getStdout();
                                preprocessedSource.preprocessorStderr = compilerRunner.getStderr();
                            } catch (ProcessCanceledException canceledException) {
                                // empty
                            } catch (Exception exception) {
                                preprocessedSource.preprocessorException = exception;
                            }
                        } else {
                            preprocessedSource.preprocessedText = sourceText;
                        }
                    }
                }
                ApplicationManager.getApplication().invokeLater(() -> preprocessedSourceConsumer.accept(preprocessedSource));
            }
        });
    }

    @NotNull
    private static String[] getPreprocessorCommandLine(@NotNull Project project, @NotNull SourceSettings sourceSettings, @NotNull String additionalSwitches, @NotNull String ignoreSwitches) {
        return Stream.concat(
                Stream.concat(
                        Stream.concat(
                                Stream.concat(Stream.of(sourceSettings.compilerPath),
                                    Stream.of(
                                            Paths.get(sourceSettings.sourcePath).getParent().toString(),
                                            project.getBasePath() != null ? project.getBasePath() : null
                                    ).filter(Objects::nonNull).distinct().map(path -> PathNormalizer.resolvePathFromLocalToCompilerHost(path, sourceSettings.host)).map(path -> "-I" + path)
                                ),
                                Stream.concat(
                                        sourceSettings.switches.stream(),
                                        AdditionalSwitches.INSTANCE.stream()
                                )
                        ),
                        Arrays.stream(additionalSwitches.split(" "))
                ).filter(x -> !Arrays.asList(ignoreSwitches.split(" ")).contains(x)),
                Stream.of(
                        "-E",
                        "-o", "-",
                        sourceSettings.languageSwitch,
                        "-c", "-"
                )
        ).filter(s -> !s.isEmpty()).toArray(String[]::new);
    }
}
