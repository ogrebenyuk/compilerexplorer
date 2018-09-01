package com.compilerexplorer;

import com.compilerexplorer.base.Explorer;
import com.compilerexplorer.base.Gui;
import com.compilerexplorer.explorer.CompilerExplorer;
import com.compilerexplorer.gui.CompilerExplorerToolWindowForm;
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
        Gui form = new CompilerExplorerToolWindowForm(project);
        Explorer explorer = new CompilerExplorer(project);
        form.setRefreshClickHandler(explorer::refresh);
        explorer.setTextConsumer(form::setMainText);
        return form.getContent();
    }

    private static void addContentToToolWindow(@NotNull JComponent content, @NotNull ToolWindow toolWindow) {
        toolWindow.getContentManager().addContent(ContentFactory.SERVICE.getInstance().createContent(content, "", false));
    }
}
