package com.compilerexplorer.compiler;

import com.compilerexplorer.common.*;
import com.compilerexplorer.common.component.BaseComponent;
import com.compilerexplorer.common.component.CEComponent;
import com.compilerexplorer.common.component.DataHolder;
import com.compilerexplorer.datamodel.CompilerResult;
import com.compilerexplorer.datamodel.SelectedSource;
import com.compilerexplorer.datamodel.SelectedSourceCompiler;
import com.compilerexplorer.datamodel.SourceSettings;
import com.compilerexplorer.datamodel.state.LocalCompilerPath;
import com.compilerexplorer.datamodel.state.LocalCompilerSettings;
import com.compilerexplorer.datamodel.state.SettingsState;
import com.compilerexplorer.compiler.common.CompilerRunner;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.stream.Stream;

public class CompilerSettingsProducer extends BaseComponent {
    private static final Logger LOG = Logger.getInstance(CompilerSettingsProducer.class);

    @NotNull
    private final Project project;
    @NotNull
    private final TaskRunner taskRunner;
    boolean needRefreshNext;

    public CompilerSettingsProducer(@NotNull CEComponent nextComponent, @NotNull Project project_, @NotNull TaskRunner taskRunner_) {
        super(nextComponent);
        LOG.debug("created");

        project = project_;
        taskRunner = taskRunner_;
    }

    @Override
    public void doClear(@NotNull DataHolder data) {
        LOG.debug("doClear");
        data.remove(SelectedSourceCompiler.KEY);
    }

    @Override
    public void doReset(@NotNull DataHolder data) {
        LOG.debug("doReset");
        SettingsState state = CompilerExplorerSettingsProvider.getInstance(project).getState();
        state.clearLocalCompilerSettings();
    }

    @Override
    public void doRefresh(@NotNull DataHolder data) {
        LOG.debug("doRefresh");
        needRefreshNext = true;
        SettingsState state = CompilerExplorerSettingsProvider.getInstance(project).getState();
        data.get(SelectedSource.KEY).ifPresentOrElse(selectedSource -> {
            SourceSettings source = selectedSource.getSelectedSource();
            LocalCompilerSettings cachedSettings = state.getLocalCompilerSettings().get(new LocalCompilerPath(source.compilerPath));
            if (cachedSettings != null) {
                LOG.debug("found cached " + source.compilerPath + " -> " + cachedSettings.getName() + " " + cachedSettings.getVersion());
                data.put(SelectedSourceCompiler.KEY, new SelectedSourceCompiler(true, false, true, null, cachedSettings));
            } else {
                needRefreshNext = false;
                taskRunner.runTask(new Task.Backgroundable(project, "Determining compiler version for " + selectedSource.getSelectedSource().sourceName) {
                    @Override
                    public void run(@NotNull ProgressIndicator indicator) {
                        boolean canceled = false;
                        boolean isSupportedCompilerType = true;
                        CompilerResult.Output output = null;
                        File workingDir = source.compilerWorkingDir;
                        String[] versionCommandLine = getVersionCommandLine(source);
                        LocalCompilerSettings localCompilerSettings = null;
                        if (isSupportedCompilerType(source.compilerKind)) {
                            int exitCode = -1;
                            String stdout = "";
                            String stderr = "";
                            Exception exception = null;
                            try {
                                CompilerRunner versionRunner = new CompilerRunner(source.host, versionCommandLine, workingDir, "", indicator, state.getCompilerTimeoutMillis());
                                exitCode = versionRunner.getExitCode();
                                stdout = versionRunner.getStdout();
                                stderr = versionRunner.getStderr();

                                String versionText = versionRunner.getStderr();
                                if (versionRunner.getExitCode() == 0 && !versionText.isEmpty()) {
                                    String compilerVersion = parseCompilerVersion(source.compilerKind, versionText);
                                    String compilerTarget = parseCompilerTarget(versionText);
                                    if (!compilerVersion.isEmpty() && !compilerTarget.isEmpty()) {
                                        LOG.debug("parsed \"" + compilerVersion + "\" \"" + compilerTarget + "\"");
                                        localCompilerSettings = new LocalCompilerSettings(source.compilerKind, compilerVersion, compilerTarget);
                                    } else {
                                        LOG.debug("bad parse \"" + compilerVersion + "\" \"" + compilerTarget + "\"");
                                    }
                                } else {
                                    LOG.debug("bad exit " + versionRunner.getExitCode());
                                }
                            } catch (ProcessCanceledException canceledException) {
                                LOG.debug("canceled");
                                canceled = true;
                            } catch (Exception exception_) {
                                LOG.debug("exception " + exception_);
                                exception = exception_;
                            }
                            output = new CompilerResult.Output(exitCode, stdout, stderr, exception);
                        } else {
                            LOG.debug("unsupported compiler kind " + source.compilerKind);
                            isSupportedCompilerType = false;
                        }
                        CompilerResult result = new CompilerResult(workingDir, versionCommandLine, output);
                        if (localCompilerSettings != null) {
                            state.addToLocalCompilerSettings(new LocalCompilerPath(source.compilerPath), localCompilerSettings);
                        }
                        data.put(SelectedSourceCompiler.KEY, new SelectedSourceCompiler(true, canceled, isSupportedCompilerType, result, localCompilerSettings));
                        CompilerSettingsProducer.super.refreshNext(data);
                    }
                });
            }
        }, () -> LOG.debug("cannot find input"));
        if (needRefreshNext) {
            CompilerSettingsProducer.super.refreshNext(data);
        }
    }

    @Override
    protected void refreshNext(@NotNull DataHolder data) {
        // empty
    }

    @NotNull
    private static String[] getVersionCommandLine(@NotNull SourceSettings sourceSettings) {
        return Stream.of(
                sourceSettings.compilerPath,
                "-v"
        ).toArray(String[]::new);
    }

    private static boolean isSupportedCompilerType(@NotNull String compilerKind) {
        return compilerKind.equals("GCC") || compilerKind.equals("Clang");
    }

    @NotNull
    private static String parseCompilerVersion(@NotNull String compilerKind, @NotNull String versionText) {
        return versionText.replace('\n', ' ').replaceAll(".*" + compilerKind.toLowerCase() + " version ([^ ]*).*", "$1");
    }

    @NotNull
    private static String parseCompilerTarget(@NotNull String versionText) {
        return versionText.replace('\n', ' ').replaceAll(".*Target: ([^-]*).*", "$1");
    }
}
