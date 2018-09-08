package com.compilerexplorer;

import com.compilerexplorer.common.datamodel.state.StateListener;
import com.compilerexplorer.compiler.SourceRemoteMatcher;
import com.compilerexplorer.compiler.CompilerSettingsProducer;
import com.compilerexplorer.explorer.RemoteCompiler;
import com.compilerexplorer.compiler.SourcePreprocessor;
import com.compilerexplorer.explorer.RemoteDefinesProducer;
import com.compilerexplorer.gui.ToolWindowGui;
import com.compilerexplorer.project.ProjectListener;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.project.Project;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class ToolWindowFactory implements com.intellij.openapi.wm.ToolWindowFactory {

    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        toolWindow.setIcon(IconLoader.getIcon("/icons/toolWindow.png"));
        addComponentToToolWindow(toolWindow, createComponent(project));
    }

    @NotNull
    private static JComponent createComponent(@NotNull Project project) {
        ToolWindowGui form = new ToolWindowGui(project);

        ProjectListener projectListener = new ProjectListener(project, form);

        SourceRemoteMatcher sourceRemoteMatcher = new SourceRemoteMatcher(project, form);
        new StateListener(project, sourceRemoteMatcher);
        CompilerSettingsProducer compilerSettingsProducer = new CompilerSettingsProducer(project, sourceRemoteMatcher);

        form.setSourceSettingsConsumer(compilerSettingsProducer);

        RemoteCompiler explorer = new RemoteCompiler(project, form);
        SourcePreprocessor preprocessor = new SourcePreprocessor(project, explorer);
        new StateListener(project, preprocessor);
        RemoteDefinesProducer remoteDefinesProducer = new RemoteDefinesProducer(project, preprocessor);

        form.setSourceRemoteMatchedConsumer(remoteDefinesProducer);

        projectListener.refresh();

        return form.getContent();
    }

    private static void addComponentToToolWindow(@NotNull ToolWindow toolWindow, @NotNull JComponent component) {
        Content content = ContentFactory.SERVICE.getInstance().createContent(component, "", false);
        toolWindow.getContentManager().addContent(content);
    }
}
