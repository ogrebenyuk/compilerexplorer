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
    private JPanel mainPanel;
    private EditorTextField editor;

    CompilerExplorerToolWindowForm(@NotNull Project project) {
        editor = createEditor(project);
        addEditorToMainPanel();
    }

    @NotNull
    JComponent getContent() {
        return content;
    }

    @NotNull
    JButton getRefreshButton() {
        return refreshButton;
    }

    @NotNull
    static private EditorTextField createEditor(@NotNull Project project) {
        return new EditorTextField(EditorFactory.getInstance().createDocument("text"), project, PlainTextFileType.INSTANCE, true, false);
    }

    private void addEditorToMainPanel() {
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(editor, BorderLayout.CENTER);
    }
}
