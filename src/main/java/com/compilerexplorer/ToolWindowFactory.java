package com.compilerexplorer;

import com.compilerexplorer.common.RefreshSignal;
import com.compilerexplorer.common.SettingsProvider;
import com.compilerexplorer.common.TaskRunner;
import com.compilerexplorer.datamodel.PreprocessedSource;
import com.compilerexplorer.datamodel.SourceCompilerSettings;
import com.compilerexplorer.datamodel.SourceRemoteMatched;
import com.compilerexplorer.datamodel.state.SettingsState;
import com.compilerexplorer.compiler.SourceRemoteMatchProducer;
import com.compilerexplorer.compiler.CompilerSettingsProducer;
import com.compilerexplorer.compiler.SourceRemoteMatchSaver;
import com.compilerexplorer.explorer.RemoteCompiler;
import com.compilerexplorer.compiler.SourcePreprocessor;
import com.compilerexplorer.explorer.RemoteCompilersProducer;
import com.compilerexplorer.gui.FormAncestorListener;
import com.compilerexplorer.gui.ToolWindowGui;
import com.compilerexplorer.project.ProjectListener;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ex.ToolWindowEx;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.function.Consumer;
import java.util.function.Function;

public class ToolWindowFactory implements com.intellij.openapi.wm.ToolWindowFactory {

    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        toolWindow.setIcon(IconLoader.getIcon("/icons/toolWindow.png"));
        addComponentToToolWindow(toolWindow, createComponent(project, toolWindow));
    }

    @NotNull
    private static JComponent createComponent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        SettingsState state = SettingsProvider.getInstance(project).getState();
        TaskRunner taskRunner = new TaskRunner();

        ToolWindowGui form = new ToolWindowGui(project, (ToolWindowEx)toolWindow);

        ProjectListener projectListener = new ProjectListener(project, form.asProjectSettingsConsumer());

        SourceRemoteMatchProducer sourceRemoteMatchProducer = new SourceRemoteMatchProducer(project, form.asSourceRemoteMatchedConsumer());
        RemoteCompilersProducer<SourceCompilerSettings> remoteCompilersProducer = new RemoteCompilersProducer<>(project, state, sourceRemoteMatchProducer, form.asErrorConsumer(), taskRunner);
        CompilerSettingsProducer compilerSettingsProducer = new CompilerSettingsProducer(project, remoteCompilersProducer, form.asErrorConsumer(), taskRunner);

        form.setSourceSettingsConsumer(compilerSettingsProducer);

        RemoteCompiler explorer = new RemoteCompiler(project, form.asCompiledTextConsumer(), form.asErrorConsumer(), taskRunner);
        SourceRemoteMatchSaver<PreprocessedSource> sourceRemoteMatchSaver2 = new SourceRemoteMatchSaver<>(project, explorer, PreprocessedSource::getSourceRemoteMatched);

        form.setPreprocessedSourceConsumer(sourceRemoteMatchSaver2);

        SourcePreprocessor preprocessor = new SourcePreprocessor(project, sourceRemoteMatchSaver2, form.asErrorConsumer(), taskRunner);
        SourceRemoteMatchSaver<SourceRemoteMatched> sourceRemoteMatchSaver1 = new SourceRemoteMatchSaver<>(project, preprocessor, Function.identity());

        form.setSourceRemoteMatchedConsumer(sourceRemoteMatchSaver1);

        Consumer<RefreshSignal> resetter = refreshSignal -> {
            switch(refreshSignal) {
                case RESET:
                    compilerSettingsProducer.asRefreshSignalConsumer().accept(refreshSignal);
                    form.asResetSignalConsumer().accept(refreshSignal);
                    explorer.asResetSignalConsumer().accept(refreshSignal);
                case RECONNECT:
                    remoteCompilersProducer.asRefreshSignalConsumer().accept(refreshSignal);
                    sourceRemoteMatchSaver1.asRefreshSignalConsumer().accept(refreshSignal);
                    sourceRemoteMatchSaver2.asRefreshSignalConsumer().accept(refreshSignal);
                    form.asReconnectSignalConsumer().accept(refreshSignal);
                case PREPROCESS:
                case COMPILE:
                    form.asRecompileSignalConsumer().accept(refreshSignal);
            }
        };
        Consumer<RefreshSignal> refresher = refreshSignal -> {
            switch(refreshSignal) {
                case RESET:
                    projectListener.refresh();
                    break;
                case RECONNECT:
                    remoteCompilersProducer.refresh();
                    break;
                case PREPROCESS:
                    preprocessor.refresh();
                    break;
                case COMPILE:
                    explorer.refresh();
                    break;
            }
        };
        Consumer<RefreshSignal> refreshSignalConsumer = refreshSignal -> {
            RefreshSignal signal = upgradeSignalIfDisconnected(state, refreshSignal);
            resetter.accept(signal);
            refresher.accept(signal);
        };
        form.setRefreshSignalConsumer(refreshSignalConsumer);
        SettingsProvider.getInstance(project).setRefreshSignalConsumer(refreshSignalConsumer);

        refresher.accept(RefreshSignal.RESET);

        new FormAncestorListener(form.getContent(), new Runnable() {
            private boolean lastEnabled = toolWindow.isVisible();
            @Override
            public void run() {
                ApplicationManager.getApplication().invokeLater(() -> {
                    try {
                        boolean enabled = toolWindow.isVisible();
                        if (enabled != lastEnabled) {
                            lastEnabled = enabled;
                            state.setEnabled(enabled);
                            if (enabled) {
                                refresher.accept(RefreshSignal.RESET);
                            }
                        }
                    } catch (Exception e) {
                        // empty
                    }
                });
            }
        });

        return form.getContent();
    }

    @NotNull
    private static RefreshSignal upgradeSignalIfDisconnected(@NotNull SettingsState state, @NotNull RefreshSignal signal) {
        if (signal != RefreshSignal.RESET && !state.getConnected()) {
            return RefreshSignal.RECONNECT;
        }
        return signal;
    }

    private static void addComponentToToolWindow(@NotNull ToolWindow toolWindow, @NotNull JComponent component) {
        Content content = ContentFactory.SERVICE.getInstance().createContent(component, "", false);
        toolWindow.getContentManager().addContent(content);
    }
}
