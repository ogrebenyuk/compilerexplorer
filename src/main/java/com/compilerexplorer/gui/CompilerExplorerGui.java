package com.compilerexplorer.gui;

import com.compilerexplorer.base.Gui;
import com.compilerexplorer.base.handlers.ClickHandler;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.fileTypes.PlainTextFileType;
import com.intellij.openapi.project.Project;
import com.intellij.ui.EditorTextField;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionListener;

public class CompilerExplorerGui implements Gui {
    @NotNull private final JPanel content;
    @NotNull private final JPanel headPanel;
    @NotNull private final JButton refreshButton;
    @NotNull private final JPanel mainPanel;
    @NotNull private final EditorTextField editor;

    private ActionListener refreshClickListener;

    public CompilerExplorerGui(@NotNull Project project) {
        content = new JPanel(new BorderLayout());
        headPanel = new JPanel(new BorderLayout());
        refreshButton = new JButton();
        refreshButton.setText("Refresh");
        headPanel.add(refreshButton, BorderLayout.WEST);
        content.add(headPanel, BorderLayout.NORTH);
        mainPanel = new JPanel(new BorderLayout());
        content.add(mainPanel, BorderLayout.CENTER);
        editor = new EditorTextField(EditorFactory.getInstance().createDocument(""), project, PlainTextFileType.INSTANCE, true, false);
        mainPanel.add(editor, BorderLayout.CENTER);
    }

    @Override
    public void setRefreshClickHandler(@NotNull ClickHandler handler) {
        if (refreshClickListener != null) {
            refreshButton.removeActionListener(refreshClickListener);
        }
        refreshClickListener = e -> handler.click();
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
}
