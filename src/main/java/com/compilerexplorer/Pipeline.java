package com.compilerexplorer;

import com.compilerexplorer.common.CompilerExplorerSettingsProvider;
import com.compilerexplorer.common.SuppressionFlag;
import com.compilerexplorer.common.TaskRunner;
import com.compilerexplorer.common.TimerScheduler;
import com.compilerexplorer.common.component.*;
import com.compilerexplorer.compiler.CompilerSettingsProducer;
import com.compilerexplorer.compiler.SourcePreprocessor;
import com.compilerexplorer.compiler.SourceRemoteMatchProducer;
import com.compilerexplorer.compiler.SourceRemoteMatchSaver;
import com.compilerexplorer.datamodel.state.SettingsState;
import com.compilerexplorer.explorer.RemoteCompiler;
import com.compilerexplorer.explorer.RemoteCompilersProducer;
import com.compilerexplorer.explorer.RemoteLibrariesProducer;
import com.compilerexplorer.gui.EditorGui;
import com.compilerexplorer.gui.MatchesGui;
import com.compilerexplorer.gui.ProjectSourcesGui;
import com.compilerexplorer.project.ProjectListener;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.util.Producer;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

public class Pipeline {
    @NonNls
    private static final Logger LOG = Logger.getInstance(Pipeline.class);
    public static final Key<Pipeline> KEY = Key.create("compilerexplorer.Pipeline");
    public static final Key<Boolean> WORKSPACE_INITIALIZED_KEY = Key.create("compilerexplorer.Pipeline.WorkspaceInitialized");
    public static final Key<Boolean> STARTED_KEY = Key.create("compilerexplorer.Pipeline.Started");

    private static class ResetInjector extends BaseLinkedComponent {
        @NonNls
        private static final Logger LOG = Logger.getInstance(ResetInjector.class);

        @NotNull
        private final ResetLevel triggerLevel;

        public ResetInjector(@NotNull CEComponent nextComponent, @NotNull ResetLevel triggerLevel_) {
            super(nextComponent);
            LOG.debug("created");

            triggerLevel = triggerLevel_;
        }

        @Override
        public void refresh(@NotNull DataHolder data) {
            boolean resetPresent = ResetFlag.in(data);
            ResetLevel level = data.get(ResetLevel.KEY).orElse(ResetLevel.NONE);
            boolean triggerReset = level == triggerLevel;
            LOG.debug("refresh looking for " + triggerLevel + ": resetPresent " + resetPresent + ", level " + level + ", triggerReset " + triggerReset);
            super.refresh(ResetFlag.with(data, resetPresent || triggerReset));
        }
    }

    @NotNull
    private final SettingsState state;
    @NotNull
    private final EditorGui editorGui;
    @NotNull
    private final RemoteCompiler explorer;
    @NotNull
    private final SourcePreprocessor preprocessor;
    @NotNull
    private final MatchesGui matchesGui;
    @NotNull
    private final RemoteCompilersProducer remoteCompilersProducer;
    @NotNull
    private final ProjectSourcesGui projectSourcesGui;
    @NotNull
    private final Initiator initiator;
    private boolean resetRequested;
    private boolean reconnectRequested;
    private boolean preprocessRequested;
    private boolean compileRequested;
    private boolean refreshRequested;
    private boolean editorReady;
    private boolean enabled = true;
    private boolean workspaceInitialized = false;
    private boolean startupHappened = false;
    private boolean firstRefreshDone;
    @NotNull
    private final TimerScheduler refreshTimerScheduler = new TimerScheduler();

    public Pipeline(@NotNull Project project) {
        LOG.debug("created");

        project.putUserData(Pipeline.KEY, this);

        state = CompilerExplorerSettingsProvider.getInstance(project).getState();

        TaskRunner taskRunner = new TaskRunner();
        SuppressionFlag suppressUpdates = new SuppressionFlag();

        editorGui = new EditorGui(data -> setPipelineReady(true), project, suppressUpdates, this::editorReady);
        explorer = new RemoteCompiler(editorGui, project, taskRunner);
        ResetInjector compileResetInjector = new ResetInjector(explorer, ResetLevel.COMPILE);
        preprocessor = new SourcePreprocessor(compileResetInjector, project, taskRunner);
        ResetInjector preprocessResetInjector = new ResetInjector(preprocessor, ResetLevel.PREPROCESS);
        SourceRemoteMatchSaver sourceRemoteMatchSaver = new SourceRemoteMatchSaver(preprocessResetInjector, project);
        matchesGui = new MatchesGui(sourceRemoteMatchSaver, project, suppressUpdates);
        SourceRemoteMatchProducer sourceRemoteMatchProducer = new SourceRemoteMatchProducer(matchesGui, project);
        CompilerSettingsProducer compilerSettingsProducer = new CompilerSettingsProducer(sourceRemoteMatchProducer, project, taskRunner);
        RemoteLibrariesProducer remoteLibrariesProducer = new RemoteLibrariesProducer(compilerSettingsProducer, project, state, taskRunner);
        remoteCompilersProducer = new RemoteCompilersProducer(remoteLibrariesProducer, project, state, state::addToUrlHistory, taskRunner);
        ResetInjector reconnectResetInjector = new ResetInjector(remoteCompilersProducer, ResetLevel.RECONNECT);
        projectSourcesGui = new ProjectSourcesGui(reconnectResetInjector, suppressUpdates);
        ProjectListener projectListener = new ProjectListener(projectSourcesGui, project);
        ResetInjector resetResetInjector = new ResetInjector(projectListener, ResetLevel.RESET);
        initiator = new Initiator(resetResetInjector);

        @Nullable Boolean workspaceInitialized_ = project.getUserData(WORKSPACE_INITIALIZED_KEY);
        if (workspaceInitialized_ != null) {
            workspaceInitialized = workspaceInitialized_;
        }

        @Nullable Boolean started_ = project.getUserData(STARTED_KEY);
        if (started_ != null) {
            startupHappened = started_;
        }
    }

    @NotNull
    public Component getEditorGuiComponent() {
        return editorGui.getComponent();
    }

    @NotNull
    public Component getMatchesGuiComponent() {
        return matchesGui.getComponent();
    }

    @NotNull
    public Component getProjectSourcesGuiComponent() {
        return projectSourcesGui.getComponent();
    }

    @NotNull
    public Producer<EditorEx> getEditorProducer() {
        return editorGui::getEditor;
    }

    private void setPipelineReady(boolean ready) {
        editorGui.setSpinningIndicatorVisible(!ready);
    }

    public void reset() {
        if (readyToRun()) {
            LOG.debug("reset");
            setPipelineReady(false);
            initiator.refresh(false, ResetLevel.RESET);
            firstRefreshDone = true;
        } else {
            LOG.debug("reset requested for later");
            resetRequested = true;
        }
    }

    public void reconnect() {
        if (readyToRun()) {
            if (firstRefreshDone) {
                LOG.debug("reconnect");
                setPipelineReady(false);
                remoteCompilersProducer.refresh(true);
            } else {
                LOG.debug("refresh with reset on reconnect");
                setPipelineReady(false);
                initiator.refresh(false, ResetLevel.RECONNECT);
                firstRefreshDone = true;
            }
        } else {
            LOG.debug("reconnect requested for later");
            reconnectRequested = true;
        }
    }

    public void preprocess() {
        if (readyToRun()) {
            if (state.getConnected()) {
                if (firstRefreshDone) {
                    LOG.debug("preprocess");
                    setPipelineReady(false);
                    preprocessor.refresh(true);
                } else {
                    LOG.debug("refresh with reset on preprocess");
                    setPipelineReady(false);
                    initiator.refresh(false, ResetLevel.PREPROCESS);
                    firstRefreshDone = true;
                }
            } else {
                LOG.debug("preprocess upgraded to reconnect");
                reconnect();
            }
        } else {
            LOG.debug("preprocess requested for later");
            preprocessRequested = true;
        }
    }

    public void compile() {
        if (readyToRun()) {
            if (state.getConnected()) {
                if (firstRefreshDone) {
                    LOG.debug("compile");
                    setPipelineReady(false);
                    explorer.refresh(true);
                } else {
                    LOG.debug("refresh with reset on compile");
                    setPipelineReady(false);
                    initiator.refresh(false, ResetLevel.COMPILE);
                    firstRefreshDone = true;
                }
            } else {
                LOG.debug("compile upgraded to reconnect");
                reconnect();
            }
        } else {
            LOG.debug("compile requested for later");
            compileRequested = true;
        }
    }

    public void refresh() {
        if (readyToRun()) {
            LOG.debug("refresh");
            setPipelineReady(false);
            initiator.refresh(false);
            firstRefreshDone = true;
        } else {
            LOG.debug("refresh requested for later");
            refreshRequested = true;
        }
    }

    public void scheduleRefresh() {
        if (readyToRun()) {
            LOG.debug("scheduleRefresh");
            refreshTimerScheduler.schedule(() -> {
                LOG.debug("running earlier scheduled refresh");
                refresh();
            }, state.getDelayMillis());
        } else {
            LOG.debug("scheduleRefresh requested for later");
            refreshRequested = true;
        }
    }

    private void editorReady() {
        if (!editorReady) {
            LOG.debug("editorReady");
            editorReady = true;
            runQueuedRequests();
        }
    }

    public void setEnabled(boolean enabled_) {
        LOG.debug("enabled " + enabled_);
        enabled = enabled_;
        if (enabled) {
            runQueuedRequests();
        }
    }

    public void workspaceInitialized() {
        LOG.debug("workspaceInitialized " + workspaceInitialized + " -> true");
        refreshTimerScheduler.schedule(() -> {
            LOG.debug("running earlier scheduled startup activity");
            workspaceInitialized = true;
            runQueuedRequests();
        }, state.getDelayMillis());
    }

    public void startupHappened() {
        LOG.debug("startupHappened " + startupHappened + " -> true");
        refreshTimerScheduler.schedule(() -> {
            LOG.debug("running earlier scheduled startup activity");
            startupHappened = true;
            runQueuedRequests();
        }, state.getDelayMillis());
    }

    private boolean readyToRun() {
        return editorReady && enabled && workspaceInitialized && startupHappened;
    }

    private void runQueuedRequests() {
        if (readyToRun()) {
            if (resetRequested) {
                LOG.debug("running requested reset");
                cleanRequests();
                reset();
            } else if (reconnectRequested) {
                LOG.debug("running requested reconnect");
                cleanRequests();
                reconnect();
            } else if (preprocessRequested) {
                LOG.debug("running requested preprocess");
                cleanRequests();
                preprocess();
            } else if (compileRequested) {
                LOG.debug("running requested compile");
                cleanRequests();
                compile();
            } else if (refreshRequested) {
                LOG.debug("running requested refresh");
                cleanRequests();
                refresh();
            } else {
                LOG.debug("nothing to do");
            }
        }
    }

    private void cleanRequests() {
        resetRequested = false;
        reconnectRequested = false;
        preprocessRequested = false;
        compileRequested = false;
        refreshRequested = false;
    }
}
