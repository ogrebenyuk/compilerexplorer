package com.compilerexplorer.gui;

import com.compilerexplorer.base.ClickHandler;
import com.compilerexplorer.base.Gui;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.fileTypes.PlainTextFileType;
import com.intellij.openapi.project.Project;
import com.intellij.ui.EditorTextField;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class CompilerExplorerToolWindowForm implements Gui {
    private JButton refreshButton;
    private JPanel content;
    private JPanel mainPanel;
    private EditorTextField editor;

    ActionListener refreshClickListener;

    public CompilerExplorerToolWindowForm(@NotNull Project project) {
        editor = createEditor(project);
        addEditorToMainPanel();
    }

    @Override
    public void setRefreshClickHandler(@NotNull ClickHandler handler) {
        if (refreshClickListener != null) {
            refreshButton.removeActionListener(refreshClickListener);
        }
        refreshClickListener = e -> handler.onClick();
        refreshButton.addActionListener(refreshClickListener);
    }

    @Override
    @NotNull
    public JComponent getContent() {
        return content;
    }

    @Override
    public void setMainText(@NotNull String text) {
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
