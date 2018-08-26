import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.PlainTextFileType;
import com.intellij.openapi.project.Project;
import com.intellij.ui.EditorTextField;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class CompilerExplorerToolWindowForm {
    private JButton refreshButton;
    private JPanel content;
    private JPanel headerPanel;
    private JPanel mainPanel;
    private EditorTextField editor;

    CompilerExplorerToolWindowForm(Project project) {
        editor = new EditorTextField(EditorFactory.getInstance().createDocument("text"), project, PlainTextFileType.INSTANCE, true, false);
    }

    void init() {
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(editor, BorderLayout.CENTER);
    }

    @NotNull
    JPanel getContent() {
        return content;
    }

    @NotNull
    JButton getRefreshButton() {
        return refreshButton;
    }
}
