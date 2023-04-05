package com.compilerexplorer;

import com.compilerexplorer.common.*;
import com.compilerexplorer.datamodel.state.SettingsState;
import com.compilerexplorer.gui.ToolWindowGui;
import com.compilerexplorer.gui.listeners.EditorChangeListener;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.project.Project;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.List;

public class CompilerExplorerToolWindowFactory implements com.intellij.openapi.wm.ToolWindowFactory {
    @NonNls
    private static final Logger LOG = Logger.getInstance(CompilerExplorerToolWindowFactory.class);

    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        addComponentToToolWindow(toolWindow, createComponent(project, toolWindow));
    }

    @NotNull
    private static JComponent createComponent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        SettingsState state = CompilerExplorerSettingsProvider.getInstance(project).getState();

        Pipeline pipeline = new Pipeline(project);

        ToolWindowGui toolWindowGui = new ToolWindowGui(project, pipeline.getProjectSourcesGuiComponent(), pipeline.getMatchesGuiComponent(), pipeline.getEditorGuiComponent(), pipeline::preprocess);

        toolWindow.setAdditionalGearActions(new DefaultActionGroup(ActionManager.getInstance().getAction("compilerexplorer.ToolbarSettingsGroup")));
        toolWindow.setTitleActions(List.of(ActionManager.getInstance().getAction("compilerexplorer.ScrollFromSource")));

        new EditorChangeListener(project, pipeline.getEditorProducer(), toolWindowGui::programTextChanged);

        CompilerExplorerSettingsProvider.getInstance(project).setReconnectRequest(pipeline::reconnect);
        CompilerExplorerSettingsProvider.getInstance(project).setPreprocessRequest(pipeline::preprocess);

        pipeline.refresh();

        new FormAncestorListener(toolWindowGui.getContent(), new LaterRunnableOnFlagChange(toolWindow::isVisible, visible -> {
            LOG.debug("visibility changed to " + visible);
            state.setEnabled(visible);
            pipeline.setEnabled(visible);
        }));

        return toolWindowGui.getContent();
    }

    private static void addComponentToToolWindow(@NotNull ToolWindow toolWindow, @NotNull JComponent component) {
        ContentFactory contentFactory = ApplicationManager.getApplication().getService(ContentFactory.class);
        Content content = contentFactory.createContent(component, "", false);
        toolWindow.getContentManager().addContent(content);
    }
}
