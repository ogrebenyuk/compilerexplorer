package com.compilerexplorer.compiler;

import com.compilerexplorer.common.*;
import com.compilerexplorer.common.component.BaseRefreshableComponent;
import com.compilerexplorer.common.component.CEComponent;
import com.compilerexplorer.common.component.DataHolder;
import com.compilerexplorer.common.compilerkind.CompilerKind;
import com.compilerexplorer.common.compilerkind.CompilerKindFactory;
import com.compilerexplorer.datamodel.*;
import com.compilerexplorer.datamodel.state.SettingsState;
import com.compilerexplorer.compiler.common.CompilerRunner;
import com.google.common.collect.Streams;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Stream;

public class SourcePreprocessor extends BaseRefreshableComponent {
    @NonNls
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
            @Nullable VirtualFile virtualFile = VfsUtil.findFile(Path.of(sourceSettings.sourcePath), true);
            @Nullable Document document = virtualFile != null ? FileDocumentManager.getInstance().getDocument(virtualFile) : null;
            @Nullable String content = document != null ? document.getText() : null;
            if (content != null) {
                @NonNls String sourceText = "# 1 \"" + sourceSettings.sourcePath.replaceAll("\\\\", "\\\\\\\\") + "\"\n" + content;
                if (state.getPreprocessLocally()) {
                    needRefreshNext = false;
                    taskRunner.runTask(new Task.Backgroundable(project, Bundle.format("compilerexplorer.SourcePreprocessor.TaskTitle", "Source", sourceSettings.sourceName)) {
                        @Override
                        public void run(@NotNull ProgressIndicator indicator) {
                            boolean canceled = false;
                            @Nullable File workingDir = sourceSettings.compilerWorkingDir.isEmpty() ? null : new File(sourceSettings.compilerWorkingDir);
                            @Nullable CompilerKind compilerKind = CompilerKindFactory.findCompilerKind(sourceSettings.compilerKind).orElse(null);
                            String[] preprocessorCommandLine = getPreprocessorCommandLine(project, sourceSettings, state.getAdditionalSwitches(), state.getIgnoreSwitches(), compilerKind);
                            int exitCode = -1;
                            String stdout = "";
                            String stderr = "";
                            Exception exception = null;
                            boolean exitCodeGood = false;
                            try {
                                LOG.debug("preprocessing " + CommandLineUtil.formCommandLine(List.of(preprocessorCommandLine)));
                                CompilerRunner compilerRunner = new CompilerRunner(sourceSettings.host, preprocessorCommandLine, workingDir, sourceText, indicator, state.getCompilerTimeoutMillis());
                                indicator.checkCanceled();
                                exitCode = compilerRunner.getExitCode();
                                stdout = compilerRunner.getStdout();
                                stderr = compilerRunner.getStderr();
                                exitCodeGood = exitCode == 0;
                                if (compilerKind != null && compilerKind.twoPassPreprocessor() && exitCodeGood) {
                                    List<String> secondPassOptions = compilerKind.getSecondPassPreprocessOptions(stderr);
                                    if (!secondPassOptions.isEmpty()) {
                                        CompilerRunner compilerRunner2 = new CompilerRunner(sourceSettings.host, secondPassOptions.toArray(new String[0]), workingDir, sourceText, indicator, state.getCompilerTimeoutMillis());
                                        indicator.checkCanceled();
                                        exitCode = compilerRunner2.getExitCode();
                                        stdout = compilerRunner2.getStdout();
                                        stderr = compilerRunner2.getStderr();
                                        exitCodeGood = exitCode == 0;
                                    }
                                }
                                LOG.debug("preprocessed " + sourceSettings.sourcePath);
                            } catch (ProcessCanceledException canceledException) {
                                LOG.debug("canceled");
                                canceled = true;
                            } catch (Exception exception_) {
                                LOG.debug("exception " + exception_);
                                exception = exception_;
                            }
                            CompilerResult.Output output = new CompilerResult.Output(exitCode, stdout, stderr, exception);
                            CompilerResult result = new CompilerResult(sourceSettings.compilerWorkingDir, preprocessorCommandLine, output);
                            data.put(PreprocessedSource.KEY, new PreprocessedSource(state.getPreprocessLocally(), canceled, result, exitCodeGood ? stdout : null));
                            SourcePreprocessor.super.refreshNext(data);
                        }
                    });
                } else {
                    LOG.debug("not preprocessed");
                    data.put(PreprocessedSource.KEY, new PreprocessedSource(false, false, null, sourceText));
                }
            } else {
                LOG.debug("cannot get content from " + sourceSettings.sourceName);
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
    private static String @NonNls @NotNull [] getPreprocessorCommandLine(@NotNull Project project,
                                                                         @NotNull SourceSettings sourceSettings,
                                                                         @NonNls @NotNull String additionalSwitches,
                                                                         @NonNls @NotNull String ignoreSwitches,
                                                                         @Nullable CompilerKind compilerKind) {
        @NotNull String includeOption = compilerKind != null ? compilerKind.getIncludeOption() : CompilerKind.DEFAULT_INCLUDE_OPTION;
        @NotNull List<String> preprocessOptions = compilerKind != null ? compilerKind.getPreprocessOptions() : CompilerKind.DEFAULT_PREPROCESS_OPTIONS;
        return Streams.concat(
            Streams.concat(
                Stream.of(sourceSettings.compilerPath),
                Stream.of(
                    Paths.get(sourceSettings.sourcePath).getParent().toString(),
                    project.getBasePath() != null ? project.getBasePath() : null
                ).filter(Objects::nonNull).distinct().map(path -> PathNormalizer.resolvePathFromLocalToCompilerHost(path, sourceSettings.host)).map(path -> includeOption + path),
                sourceSettings.switches.stream(),
                compilerKind != null ? compilerKind.additionalSwitches().stream() : Stream.empty(),
                CommandLineUtil.parseCommandLine(additionalSwitches).stream()
            ).filter(x -> !CommandLineUtil.parseCommandLine(ignoreSwitches).contains(x)),
            Streams.concat(
                CommandLineUtil.parseCommandLine(sourceSettings.languageSwitch).stream(),
                preprocessOptions.stream()
            )
        ).filter(s -> !s.isEmpty()).toArray(String[]::new);
    }
}
