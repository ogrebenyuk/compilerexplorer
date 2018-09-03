package com.compilerexplorer.settings;

import com.compilerexplorer.common.CompilerExplorerState;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
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

    CompilerExplorerSettingsGui(@NotNull Project project_) {
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
        mainPanel.add(url, BorderLayout.CENTER);
        JButton connectButton = new JButton();
        connectButton.setText("Connect");
        connectButton.addActionListener(e -> CompilerExplorerConnection.connect(project, state));
        mainPanel.add(connectButton, BorderLayout.EAST);
    }

    void loadState(@NotNull CompilerExplorerState state_) {
        state.copyFrom(state_);
        url.setText(state.getUrl());
    }

    @NotNull
    JComponent getContent() {
        return content;
    }

    @NotNull
    CompilerExplorerState getState() {
        state.setUrl(url.getText());
        return state;
    }
}
