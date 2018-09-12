package com.compilerexplorer.settings.gui;

import com.compilerexplorer.common.TaskRunner;
import com.compilerexplorer.common.datamodel.state.SettingsState;
import com.compilerexplorer.explorer.RemoteCompilersProducer;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.panels.VerticalLayout;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;

public class SettingsGui {
    private static final int GAP = 2;

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
    @NotNull
    private final TaskRunner taskRunner;

    public SettingsGui(@NotNull Project project_) {
        taskRunner = new TaskRunner();
        ignoreUpdates = true;
        project = project_;
        state = new SettingsState();
        content = new JPanel(new VerticalLayout(GAP));
        JPanel urlPanel = new JPanel(new BorderLayout(GAP, GAP));
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
                    state.setConnected(false);
                }
            }
        });
        urlPanel.add(urlField, BorderLayout.CENTER);

        JLabel testResultLabel = new JLabel();

        JButton connectButton = new JButton();
        connectButton.setText("Test connection");
        connectButton.addActionListener(e -> {
            SettingsState testState = new SettingsState();
            populateStateFromGui(testState);
            testState.setEnabled(true);
            testState.setConnected(false);
            (new RemoteCompilersProducer<Boolean>(
                    project,
                    testState,
                    unused -> testResultLabel.setText("Success: found " + testState.getRemoteCompilers().size() + " compilers"),
                    error  -> testResultLabel.setText("Error: " + error.getMessage()),
                    taskRunner
            )).accept(false);
        });
        urlPanel.add(connectButton, BorderLayout.EAST);

        content.add(urlPanel, VerticalLayout.TOP);

        JPanel testResultPanel = new JPanel(new BorderLayout(GAP, GAP));
        testResultLabel.setVisible(true);
        testResultLabel.setText("");
        testResultPanel.add(testResultLabel, BorderLayout.CENTER);

        content.add(testResultPanel, VerticalLayout.TOP);

        JPanel preprocessPanel = new JPanel(new BorderLayout(GAP, GAP));
        preprocessCheckbox = new JCheckBox();
        preprocessCheckbox.setText("Preprocess locally");
        preprocessPanel.add(preprocessCheckbox, BorderLayout.WEST);

        content.add(preprocessPanel, VerticalLayout.TOP);

        ignoreUpdates = false;
    }

    public void loadState(@NotNull SettingsState state_) {
        ignoreUpdates = true;
        state.copyFrom(state_);
        populateGuiFromState();
        ignoreUpdates = false;
    }

    @NotNull
    public JComponent getContent() {
        return content;
    }

    @NotNull
    public SettingsState getState() {
        populateStateFromGui(state);
        return state;
    }

    private void populateGuiFromState() {
        urlField.setText(state.getUrl());
        preprocessCheckbox.setSelected(state.getPreprocessLocally());
    }

    private void populateStateFromGui(@NotNull SettingsState state_) {
        state_.setUrl(urlField.getText());
        state_.setPreprocessLocally(preprocessCheckbox.isSelected());
    }

    public void reset() {
        taskRunner.reset();
    }
}
