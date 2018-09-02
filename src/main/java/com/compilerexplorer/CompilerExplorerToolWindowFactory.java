package com.compilerexplorer;

import com.compilerexplorer.explorer.CompilerExplorer;
import com.compilerexplorer.gui.CompilerExplorerGui;
import com.compilerexplorer.project.ProjectListener;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.project.Project;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class CompilerExplorerToolWindowFactory implements ToolWindowFactory {
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        addContentToToolWindow(createContent(project), toolWindow);
    }

    @NotNull
    private static JComponent createContent(@NotNull Project project) {
        CompilerExplorerGui form = new CompilerExplorerGui(project);
        ProjectListener projectListener = new ProjectListener(project, form);
        CompilerExplorer explorer = new CompilerExplorer(project, form);
        form.setSourceSettingsConsumer(explorer);
        projectListener.refresh();
        return form.getContent();
    }

    private static void addContentToToolWindow(@NotNull JComponent content, @NotNull ToolWindow toolWindow) {
        toolWindow.getContentManager().addContent(ContentFactory.SERVICE.getInstance().createContent(content, "", false));
    }
}
