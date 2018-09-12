package com.compilerexplorer;

import com.compilerexplorer.common.RefreshSignal;
import com.compilerexplorer.common.SettingsProvider;
import com.compilerexplorer.common.TaskRunner;
import com.compilerexplorer.common.datamodel.SourceCompilerSettings;
import com.compilerexplorer.common.datamodel.state.SettingsState;
import com.compilerexplorer.compiler.SourceRemoteMatchProducer;
import com.compilerexplorer.compiler.CompilerSettingsProducer;
import com.compilerexplorer.compiler.SourceRemoteMatchSaver;
import com.compilerexplorer.explorer.RemoteCompiler;
import com.compilerexplorer.compiler.SourcePreprocessor;
import com.compilerexplorer.explorer.RemoteCompilersProducer;
import com.compilerexplorer.gui.FormAncestorListener;
import com.compilerexplorer.gui.ToolWindowGui;
import com.compilerexplorer.project.ProjectListener;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ex.ToolWindowEx;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.function.Consumer;

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
        SourcePreprocessor preprocessor = new SourcePreprocessor(project, explorer, form.asErrorConsumer(), taskRunner);
        SourceRemoteMatchSaver sourceRemoteMatchSaver = new SourceRemoteMatchSaver(project, preprocessor);

        form.setSourceRemoteMatchedConsumer(sourceRemoteMatchSaver);

        Consumer<RefreshSignal> resetter = refreshSignal -> {
            System.out.println("resetter");
            switch(refreshSignal) {
                case RESET:
                    compilerSettingsProducer.asRefreshSignalConsumer().accept(refreshSignal);
                    form.asResetSignalConsumer().accept(refreshSignal);
                case RECONNECT:
                    remoteCompilersProducer.asRefreshSignalConsumer().accept(refreshSignal);
                    sourceRemoteMatchSaver.asRefreshSignalConsumer().accept(refreshSignal);
                    form.asReconnectSignalConsumer().accept(refreshSignal);
                case PREPROCESS:
                case COMPILE:
                    form.asRecompileSignalConsumer().accept(refreshSignal);
            }
        };
        Consumer<RefreshSignal> refresher = refreshSignal -> {
            System.out.println("refresher");
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
            System.out.println("refreshSignalConsumer");
            RefreshSignal signal = upgradeSignalIfDisconnected(project, state, refreshSignal);
            resetter.accept(signal);
            refresher.accept(signal);
        };
        form.setRefreshSignalConsumer(refreshSignalConsumer);
        SettingsProvider.getInstance(project).setRefreshSignalConsumer(refreshSignalConsumer);

        new FormAncestorListener(form.getContent(), new Consumer<Boolean>() {
            private boolean lastEnabled = toolWindow.isVisible();
            @Override
            public void accept(@NotNull Boolean unused) {
                boolean enabled = toolWindow.isVisible();
                if (enabled != lastEnabled) {
                    System.out.println("FormAncestorListener");
                    lastEnabled = enabled;
                    state.setEnabled(enabled);
                    if (enabled) {
                        refresher.accept(RefreshSignal.RESET);
                    }
                }
            }
        });

        refresher.accept(RefreshSignal.RESET);

        return form.getContent();
    }

    @NotNull
    private static RefreshSignal upgradeSignalIfDisconnected(@NotNull Project project, @NotNull SettingsState state, @NotNull RefreshSignal signal) {
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
