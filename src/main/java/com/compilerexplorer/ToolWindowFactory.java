package com.compilerexplorer;

import com.compilerexplorer.common.datamodel.state.StateListener;
import com.compilerexplorer.compiler.SourceRemoteMatcher;
import com.compilerexplorer.compiler.CompilerSettingsProducer;
import com.compilerexplorer.explorer.RemoteCompiler;
import com.compilerexplorer.explorer.RemoteConnectionListener;
import com.compilerexplorer.compiler.SourcePreprocessor;
import com.compilerexplorer.explorer.RemoteDefinesProducer;
import com.compilerexplorer.gui.ToolWindowGui;
import com.compilerexplorer.project.ProjectListener;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.project.Project;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class ToolWindowFactory implements com.intellij.openapi.wm.ToolWindowFactory {
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        addContentToToolWindow(toolWindow, createContent(project));
    }

    @NotNull
    private static JComponent createContent(@NotNull Project project) {
        ToolWindowGui form = new ToolWindowGui(project);
        ProjectListener projectListener = new ProjectListener(project, form);
        RemoteCompiler explorer = new RemoteCompiler(project, form);
        SourcePreprocessor preprocessor = new SourcePreprocessor(project, explorer);
        new StateListener(project, preprocessor);
        RemoteDefinesProducer remoteDefinesProducer = new RemoteDefinesProducer(project, preprocessor);
        SourceRemoteMatcher sourceRemoteMatcher = new SourceRemoteMatcher(project, remoteDefinesProducer);
        new RemoteConnectionListener(project, sourceRemoteMatcher);
        new StateListener(project, sourceRemoteMatcher);
        CompilerSettingsProducer compilerSettingsProducer = new CompilerSettingsProducer(project, sourceRemoteMatcher);

        form.setSourceSettingsConsumer(compilerSettingsProducer);
        projectListener.refresh();

        return form.getContent();
    }

    private static void addContentToToolWindow(@NotNull ToolWindow toolWindow, @NotNull JComponent content) {
        toolWindow.getContentManager().addContent(ContentFactory.SERVICE.getInstance().createContent(content, "", false));
    }
}
