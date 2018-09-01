import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.PlainTextFileType;
import com.intellij.openapi.project.Project;
import com.intellij.ui.EditorTextField;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class CompilerExplorerToolWindowForm {
    private JButton refreshButton;
    private JPanel content;
    private JPanel mainPanel;
    private EditorTextField editor;

    CompilerExplorerToolWindowForm(@NotNull Project project, @NotNull ActionListener refreshListener) {
        refreshButton.addActionListener(refreshListener);
        editor = createEditor(project);
        addEditorToMainPanel();
    }

    @NotNull
    JComponent getContent() {
        return content;
    }

    void setText(@NotNull String text) {
        editor.setText(text);
    }

    @NotNull
    static private EditorTextField createEditor(@NotNull Project project) {
        return new EditorTextField(EditorFactory.getInstance().createDocument(""), project, PlainTextFileType.INSTANCE, true, false);
    }

    private void addEditorToMainPanel() {
        mainPanel.setLayout(new BorderLayout());
        mainPanel.add(editor, BorderLayout.CENTER);
    }
}
