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
        CompilerExplorerEvents events = new CompilerExplorerEvents();
        CompilerExplorer explorer = new CompilerExplorer(project);
        CompilerExplorerToolWindowForm form = new CompilerExplorerToolWindowForm(project, event -> events.refresh());
        events.set(explorer, form);
        return form.getContent();
    }

    private static void addContentToToolWindow(@NotNull JComponent content, @NotNull ToolWindow toolWindow) {
        toolWindow.getContentManager().addContent(ContentFactory.SERVICE.getInstance().createContent(content, "", false));
    }
}
