package com.compilerexplorer.settings;

import com.compilerexplorer.common.CompilerExplorerState;
import com.compilerexplorer.explorer.CompilerExplorerConnection;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

class CompilerExplorerSettingsGui {
    @NotNull
    private final Project project;
    @NotNull
    private final CompilerExplorerState state;
    @NotNull
    private final JPanel content;
    @NotNull
    private final JTextField url;
    private boolean ignoreUpdates;

    CompilerExplorerSettingsGui(@NotNull Project project_) {
        ignoreUpdates = true;
        project = project_;
        state = new CompilerExplorerState();
        content = new JPanel(new BorderLayout());
        JPanel mainPanel = new JPanel(new BorderLayout());
        content.add(mainPanel, BorderLayout.NORTH);
        JLabel label = new JLabel();
        label.setVisible(true);
        label.setText("URL: ");
        mainPanel.add(label, BorderLayout.WEST);
        url = new JTextField();
        url.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                // empty
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                update();
            }
            @Override
            public void insertUpdate(DocumentEvent e) {
                update();
            }
            private void update() {
                if (!ignoreUpdates) {
                    state.setUrl(url.getText());
                    state.clearConnection();
                }
            }
        });
        mainPanel.add(url, BorderLayout.CENTER);
        JButton connectButton = new JButton();
        connectButton.setText("Connect");
        connectButton.addActionListener(e -> CompilerExplorerConnection.connect(project, state, false));
        mainPanel.add(connectButton, BorderLayout.EAST);
        ignoreUpdates = false;
    }

    void loadState(@NotNull CompilerExplorerState state_) {
        state.copyFrom(state_);
        ignoreUpdates = true;
        url.setText(state.getUrl());
        ignoreUpdates = false;
    }

    @NotNull
    JComponent getContent() {
        return content;
    }

    @NotNull
    CompilerExplorerState getState() {
        return state;
    }
}
