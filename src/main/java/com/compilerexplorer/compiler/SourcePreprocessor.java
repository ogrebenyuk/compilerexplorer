package com.compilerexplorer.compiler;

import com.compilerexplorer.common.*;
import com.compilerexplorer.common.component.BaseRefreshableComponent;
import com.compilerexplorer.common.component.CEComponent;
import com.compilerexplorer.common.component.DataHolder;
import com.compilerexplorer.datamodel.*;
import com.compilerexplorer.datamodel.state.SettingsState;
import com.compilerexplorer.compiler.common.CompilerRunner;
import com.intellij.openapi.diagnostic.Logger;
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
import java.util.stream.Stream;

public class SourcePreprocessor extends BaseRefreshableComponent {
    private static final Logger LOG = Logger.getInstance(SourcePreprocessor.class);

    @NotNull
    private final Project project;
    @NotNull
    private final TaskRunner taskRunner;
    boolean needRefreshNext;

    public SourcePreprocessor(@NotNull CEComponent nextComponent, @NotNull Project project_, @NotNull TaskRunner taskRunner_) {
        super(nextComponent);
        LOG.debug("created");

        project = project_;
        taskRunner = taskRunner_;
    }

    @Override
    protected void doClear(@NotNull DataHolder data) {
        LOG.debug("doClear");
        data.remove(PreprocessedSource.KEY);
    }

    @Override
    public void doRefresh(@NotNull DataHolder data) {
        LOG.debug("doRefresh");
        needRefreshNext = true;
        SettingsState state = CompilerExplorerSettingsProvider.getInstance(project).getState();
        data.get(SelectedSource.KEY).ifPresentOrElse(selectedSource -> {
            SourceSettings sourceSettings = selectedSource.getSelectedSource();
            @Nullable Document document = FileDocumentManager.getInstance().getDocument(sourceSettings.source);
            if (document != null) {
                String sourceText = "# 1 \"" + sourceSettings.sourcePath.replaceAll("\\\\", "\\\\\\\\") + "\"\n" + document.getText();
                if (state.getPreprocessLocally()) {
                    needRefreshNext = false;
                    taskRunner.runTask(new Task.Backgroundable(project, "Preprocessing " + sourceSettings.sourceName) {
                        @Override
                        public void run(@NotNull ProgressIndicator indicator) {
                            boolean canceled = false;
                            File workingDir =  sourceSettings.compilerWorkingDir;
                            String[] preprocessorCommandLine = getPreprocessorCommandLine(project, sourceSettings, state.getAdditionalSwitches(), state.getIgnoreSwitches());
                            int exitCode = -1;
                            String stdout = "";
                            String stderr = "";
                            Exception exception = null;
                            try {
                                LOG.debug("preprocessing " + String.join(" ", preprocessorCommandLine));
                                CompilerRunner compilerRunner = new CompilerRunner(sourceSettings.host, preprocessorCommandLine, workingDir, sourceText, indicator, state.getCompilerTimeoutMillis());
                                exitCode = compilerRunner.getExitCode();
                                stdout = compilerRunner.getStdout();
                                stderr = compilerRunner.getStderr();
                                LOG.debug("preprocessed " + sourceSettings.sourcePath);
                            } catch (ProcessCanceledException canceledException) {
                                LOG.debug("canceled");
                                canceled = true;
                            } catch (Exception exception_) {
                                LOG.debug("exception " + exception_);
                                exception = exception_;
                            }
                            CompilerResult.Output output = new CompilerResult.Output(exitCode, stdout, stderr, exception);
                            CompilerResult result = new CompilerResult(workingDir, preprocessorCommandLine, output);
                            data.put(PreprocessedSource.KEY, new PreprocessedSource(state.getPreprocessLocally(), canceled, result, exitCode == 0 ? stdout : null));
                            SourcePreprocessor.super.refreshNext(data);
                        }
                    });
                } else {
                    LOG.debug("not preprocessed");
                    data.put(PreprocessedSource.KEY, new PreprocessedSource(state.getPreprocessLocally(), false, null, sourceText));
                }
            } else {
                LOG.debug("cannot get Document from " + sourceSettings.sourceName);
            }
        }, () -> LOG.debug("cannot find input"));
        if (needRefreshNext) {
            SourcePreprocessor.super.refreshNext(data);
        }
    }

    @Override
    protected void refreshNext(@NotNull DataHolder data) {
        // empty
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
