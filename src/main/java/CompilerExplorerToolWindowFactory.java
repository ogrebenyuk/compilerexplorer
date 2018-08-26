import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.project.Project;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

public class CompilerExplorerToolWindowFactory implements ToolWindowFactory {
    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        CompilerExplorer explorer = new CompilerExplorer();
        CompilerExplorerToolWindowForm form = new CompilerExplorerToolWindowForm(project);
        form.init();
        form.getRefreshButton().addActionListener(event -> explorer.refresh());

        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(form.getContent(), "", false);
        toolWindow.getContentManager().addContent(content);
    }
}
