import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ToolWindowFactory;
import com.intellij.openapi.project.Project;
import com.intellij.ui.content.Content;
import com.intellij.ui.content.ContentFactory;
import org.jetbrains.annotations.NotNull;

public class CompilerExplorerToolWindowFactory implements ToolWindowFactory {
    public CompilerExplorerToolWindowFactory() {
        Messages.showMessageDialog("CompilerExplorerToolWindowFactory()", "Event", Messages.getInformationIcon());
    }

    public void createToolWindowContent(@NotNull Project project, @NotNull ToolWindow toolWindow) {
        Messages.showMessageDialog(project, "createToolWindowContent()", "Event", Messages.getInformationIcon());

        CompilerExplorer explorer = new CompilerExplorer();
        CompilerExplorerToolWindowForm form = new CompilerExplorerToolWindowForm();

        form.getRefreshButton().addActionListener(event -> explorer.refresh());

        ContentFactory contentFactory = ContentFactory.SERVICE.getInstance();
        Content content = contentFactory.createContent(form.getContent(), "", false);
        toolWindow.getContentManager().addContent(content);
    }
}
