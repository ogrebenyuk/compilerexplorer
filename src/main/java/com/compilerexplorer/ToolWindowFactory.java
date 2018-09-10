package com.compilerexplorer;

import com.compilerexplorer.common.datamodel.state.StateListener;
import com.compilerexplorer.compiler.SourceRemoteMatcher;
import com.compilerexplorer.compiler.CompilerSettingsProducer;
import com.compilerexplorer.explorer.RemoteCompiler;
import com.compilerexplorer.compiler.SourcePreprocessor;
import com.compilerexplorer.explorer.RemoteDefinesProducer;
import com.compilerexplorer.gui.ToolWindowGui;
import com.compilerexplorer.project.ProjectListener;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.wm.FocusWatcher;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.wm.ex.ToolWindowEx;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;
import java.awt.*;
import java.awt.event.FocusEvent;

public class ToolWindowFactory implements com.intellij.openapi.wm.ToolWindowFactory {

    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        toolWindow.setIcon(IconLoader.getIcon("/icons/toolWindow.png"));
        addComponentToToolWindow(toolWindow, createComponent(project, toolWindow));
    }

    @NotNull
    private static JComponent createComponent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        ToolWindowGui form = new ToolWindowGui(project, (ToolWindowEx)toolWindow);

        ProjectListener projectListener = new ProjectListener(project, form);

        SourceRemoteMatcher sourceRemoteMatcher = new SourceRemoteMatcher(project, form);
        new StateListener(project, sourceRemoteMatcher);
        CompilerSettingsProducer compilerSettingsProducer = new CompilerSettingsProducer(project, sourceRemoteMatcher);

        form.setSourceSettingsConsumer(compilerSettingsProducer);

        RemoteCompiler explorer = new RemoteCompiler(project, form);
        new StateListener(project, explorer);
        SourcePreprocessor preprocessor = new SourcePreprocessor(project, explorer);
        new StateListener(project, preprocessor);
        RemoteDefinesProducer remoteDefinesProducer = new RemoteDefinesProducer(project, preprocessor);

        form.setSourceRemoteMatchedConsumer(remoteDefinesProducer);
        form.setRecompileConsumer(preprocessor);
        form.setRefreshConsumer(projectListener);

        form.refresh();

        /*
        form.getContent().addAncestorListener (new AncestorListener() {
            @Override
            public void ancestorAdded(AncestorEvent event) {
                System.out.println("ancestorAdded");
            }
            public void ancestorRemoved(AncestorEvent event) {
                System.out.println("ancestorRemoved");
            }
            public void ancestorMoved(AncestorEvent event) {
            }
        });
        */
        return form.getContent();
    }

    private static void addComponentToToolWindow(@NotNull ToolWindow toolWindow, @NotNull JComponent component) {
        Content content = ContentFactory.SERVICE.getInstance().createContent(component, "", false);
        toolWindow.getContentManager().addContent(content);
    }
}
