package com.compilerexplorer.settings.gui;

import com.compilerexplorer.common.datamodel.state.SettingsState;
import com.compilerexplorer.common.RemoteConnection;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.panels.VerticalLayout;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

public class SettingsGui {
    @NotNull
    private final Project project;
    @NotNull
    private final SettingsState state;
    @NotNull
    private final JPanel content;
    @NotNull
    private final JTextField urlField;
    @NotNull
    private final JCheckBox preprocessCheckbox;
    private boolean ignoreUpdates;

    public SettingsGui(@NotNull Project project_) {
        ignoreUpdates = true;
        project = project_;
        state = new SettingsState();
        content = new JPanel(new VerticalLayout(2));
        JPanel urlPanel = new JPanel(new BorderLayout());
        content.add(urlPanel, VerticalLayout.TOP);
        JLabel label = new JLabel();
        label.setVisible(true);
        label.setText("Compiler Explorer URL: ");
        urlPanel.add(label, BorderLayout.WEST);
        urlField = new JTextField();
        urlField.getDocument().addDocumentListener(new DocumentListener() {
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
                    state.clearConnection();
                }
            }
        });
        urlPanel.add(urlField, BorderLayout.CENTER);
        /*
        JButton connectButton = new JButton();
        connectButton.setText("Test connection");
        connectButton.addActionListener(e -> RemoteConnection.tryConnect(project, state));
        urlPanel.add(connectButton, BorderLayout.EAST);
        */
        JPanel preprocessPanel = new JPanel(new BorderLayout());
        content.add(preprocessPanel, VerticalLayout.TOP);
        preprocessCheckbox = new JCheckBox();
        preprocessCheckbox.setText("Preprocess locally");
        preprocessPanel.add(preprocessCheckbox, BorderLayout.WEST);
        JPanel minorMismatchPanel = new JPanel(new BorderLayout());
        content.add(minorMismatchPanel, VerticalLayout.TOP);
        ignoreUpdates = false;
    }

    public void loadState(@NotNull SettingsState state_) {
        ignoreUpdates = true;
        state.copyFrom(state_);
        urlField.setText(state.getUrl());
        preprocessCheckbox.setSelected(state.getPreprocessLocally());
        ignoreUpdates = false;
    }

    @NotNull
    public JComponent getContent() {
        return content;
    }

    @NotNull
    public SettingsState getState() {
        state.setUrl(urlField.getText());
        state.setPreprocessLocally(preprocessCheckbox.isSelected());
        return state;
    }
}
