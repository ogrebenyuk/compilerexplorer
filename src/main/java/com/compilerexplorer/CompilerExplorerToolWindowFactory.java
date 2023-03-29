package com.compilerexplorer;

import com.compilerexplorer.common.*;
import com.compilerexplorer.datamodel.state.SettingsState;
import com.compilerexplorer.compiler.SourceRemoteMatchProducer;
import com.compilerexplorer.compiler.CompilerSettingsProducer;
import com.compilerexplorer.compiler.SourceRemoteMatchSaver;
import com.compilerexplorer.explorer.RemoteCompiler;
import com.compilerexplorer.compiler.SourcePreprocessor;
import com.compilerexplorer.explorer.RemoteCompilersProducer;
import com.compilerexplorer.gui.EditorGui;
import com.compilerexplorer.gui.MatchesGui;
import com.compilerexplorer.gui.ProjectSettingsGui;
import com.compilerexplorer.gui.ToolWindowGui;
import com.compilerexplorer.project.ProjectListener;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.project.Project;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;
import java.util.function.Consumer;

public class CompilerExplorerToolWindowFactory implements com.intellij.openapi.wm.ToolWindowFactory {
    private static class WaitUntilEditorReady implements Consumer<RefreshSignal> {
        private boolean ready;
        @Nullable
        private Consumer<RefreshSignal> delegate;
        @Nullable
        private RefreshSignal strongestSignal;

        public void setDelegate(@NotNull Consumer<RefreshSignal> delegate_) {
            delegate = delegate_;
            trySendSignal();
        }

        public boolean getReady() {
            return ready;
        }

        public void setReady() {
            ready = true;
            trySendSignal();
        }

        @Override
        public void accept(@NotNull RefreshSignal refreshSignal) {
            accumulate(refreshSignal);
            trySendSignal();
        }

        private void accumulate(@NotNull RefreshSignal refreshSignal) {
            if (strongestSignal == null || refreshSignal.strongerThan(strongestSignal)) {
                strongestSignal = refreshSignal;
            }
        }

        private void trySendSignal() {
            if (ready && delegate != null && strongestSignal != null) {
                RefreshSignal signal = strongestSignal;
                ApplicationManager.getApplication().invokeLater(() -> delegate.accept(signal));
                strongestSignal = null;
            }
        }
    }

    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        addComponentToToolWindow(toolWindow, createComponent(project, toolWindow));
    }

    @NotNull
    private static JComponent createComponent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        SettingsState state = CompilerExplorerSettingsProvider.getInstance(project).getState();
        TaskRunner taskRunner = new TaskRunner();
        SuppressionFlag suppressUpdates = new SuppressionFlag();
        WaitUntilEditorReady resetDelegate = new WaitUntilEditorReady();
        WaitUntilEditorReady refreshDelegate = new WaitUntilEditorReady();
        EditorGui editorGui = new EditorGui(project, suppressUpdates, () -> {resetDelegate.setReady(); refreshDelegate.setReady();});
        RemoteCompiler explorer = new RemoteCompiler(project, editorGui, taskRunner);
        SourceRemoteMatchSaver sourceRemoteMatchSaver = new SourceRemoteMatchSaver(project, explorer);
        MatchesGui matchesGui = new MatchesGui(suppressUpdates, sourceRemoteMatchSaver);
        SourceRemoteMatchProducer sourceRemoteMatchProducer = new SourceRemoteMatchProducer(project, matchesGui);
        SourcePreprocessor preprocessor = new SourcePreprocessor(project, sourceRemoteMatchProducer, taskRunner);
        CompilerSettingsProducer compilerSettingsProducer = new CompilerSettingsProducer(project, preprocessor, taskRunner);
        RemoteCompilersProducer remoteCompilersProducer = new RemoteCompilersProducer(project, state, compilerSettingsProducer, state::addToUrlHistory, taskRunner);
        ProjectSettingsGui projectSettingsGui = new ProjectSettingsGui(suppressUpdates, remoteCompilersProducer);
        ProjectListener projectListener = new ProjectListener(project, projectSettingsGui, resetDelegate::getReady);

        Consumer<RefreshSignal> resetter = refreshSignal -> {
            switch (refreshSignal) {
                case RESET:
                    compilerSettingsProducer.asResetSignalConsumer().accept(refreshSignal);
                    projectSettingsGui.asResetSignalConsumer().accept(refreshSignal);
                    explorer.asResetSignalConsumer().accept(refreshSignal);
                    // fall through
                case RECONNECT:
                    remoteCompilersProducer.asReconnectSignalConsumer().accept(refreshSignal);
                    sourceRemoteMatchSaver.asReconnectSignalConsumer().accept(refreshSignal);
                    matchesGui.asReconnectSignalConsumer().accept(refreshSignal);
                    // fall through
                case PREPROCESS:
                    // fall through
                case COMPILE:
                    // fall through
            }
        };
        resetDelegate.setDelegate(resetter);
        Consumer<RefreshSignal> refresher = refreshSignal -> {
            switch (refreshSignal) {
                case RESET -> projectListener.refresh();
                case RECONNECT -> remoteCompilersProducer.refresh();
                case PREPROCESS -> preprocessor.refresh();
                case COMPILE -> explorer.refresh();
            }
        };
        refreshDelegate.setDelegate(refresher);
        Consumer<RefreshSignal> refreshSignalConsumer = refreshSignal -> {
            RefreshSignal signal = strengthenSignalIfDisconnected(state, refreshSignal);
            resetDelegate.accept(signal);
            refreshDelegate.accept(signal);
        };

        ToolWindowGui toolWindowGui = new ToolWindowGui(project, refreshSignalConsumer, projectSettingsGui.getComponent(), matchesGui.getComponent(), editorGui.getComponent(), editorGui::getEditor);

        toolWindow.setAdditionalGearActions(new DefaultActionGroup(ActionManager.getInstance().getAction("compilerexplorer.ToolbarSettingsGroup")));
        toolWindow.setTitleActions(List.of(ActionManager.getInstance().getAction("compilerexplorer.ScrollFromSource")));

        CompilerExplorerSettingsProvider.getInstance(project).setRefreshSignalConsumer(refreshSignalConsumer);

        refreshDelegate.accept(RefreshSignal.RESET);

        new FormAncestorListener(toolWindowGui.getContent(), new LaterRunnableOnFlagChange(toolWindow::isVisible, visible -> {
            state.setEnabled(visible);
            if (visible) {
                refreshDelegate.accept(RefreshSignal.RESET);
            }
        }));

        return toolWindowGui.getContent();
    }

    @NotNull
    private static RefreshSignal strengthenSignalIfDisconnected(@NotNull SettingsState state, @NotNull RefreshSignal signal) {
        if (RefreshSignal.RECONNECT.strongerThan(signal) && !state.getConnected()) {
            return RefreshSignal.RECONNECT;
        }
        return signal;
    }

    private static void addComponentToToolWindow(@NotNull ToolWindow toolWindow, @NotNull JComponent component) {
        ContentFactory contentFactory = ApplicationManager.getApplication().getService(ContentFactory.class);
        Content content = contentFactory.createContent(component, "", false);
        toolWindow.getContentManager().addContent(content);
    }
}
