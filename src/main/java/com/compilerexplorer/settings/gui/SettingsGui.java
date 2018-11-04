package com.compilerexplorer.settings.gui;

import com.compilerexplorer.common.TaskRunner;
import com.compilerexplorer.datamodel.state.SettingsState;
import com.compilerexplorer.explorer.RemoteCompilersProducer;
import com.intellij.openapi.project.Project;
import com.intellij.ui.ColorPanel;
import com.intellij.ui.components.panels.VerticalLayout;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.util.stream.Collectors;

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
    private final ColorPanel highlightColorChooserPanel;
    @NotNull
    private final TaskRunner taskRunner;

    public SettingsGui(@NotNull Project project_) {
        taskRunner = new TaskRunner();
        ignoreUpdates = true;
        project = project_;
        state = new SettingsState();
        content = new JPanel(new VerticalLayout(GAP));
        JPanel urlPanel = new JPanel(new BorderLayout(GAP, GAP));
        JLabel urlLabel = new JLabel();
        urlLabel.setVisible(true);
        urlLabel.setText("Compiler Explorer URL: ");
        urlPanel.add(urlLabel, BorderLayout.WEST);
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
                    unused -> {
                        testResultLabel.setText("Success: found " + testState.getRemoteCompilers().size() + " compilers");
                        testResultLabel.setToolTipText(testState.getRemoteCompilers().stream().map(c -> c.getLanguage() + " " + c.getName()).collect(Collectors.joining("\n")));
                    },
                    error  -> {
                        testResultLabel.setText("Error: " + error.getMessage());
                        testResultLabel.setToolTipText("");
                    },
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

        JPanel highlightColorPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, GAP, GAP));
        JLabel highlightColorLabel = new JLabel();
        highlightColorLabel.setVisible(true);
        highlightColorLabel.setText("Highlight color: ");
        highlightColorPanel.add(highlightColorLabel);
        highlightColorChooserPanel = new ColorPanel();
        highlightColorPanel.add(highlightColorChooserPanel);

        content.add(highlightColorPanel, VerticalLayout.TOP);

        ignoreUpdates = false;
    }

    public void copyFrom(@NotNull SettingsState state_) {
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
        highlightColorChooserPanel.setSelectedColor(new Color(state.getHighlightColorRGB()));
    }

    private void populateStateFromGui(@NotNull SettingsState state_) {
        state_.setUrl(urlField.getText());
        state_.setPreprocessLocally(preprocessCheckbox.isSelected());
        Color highlightColor = highlightColorChooserPanel.getSelectedColor();
        if (highlightColor != null) {
            state_.setHighlightColorRGB(highlightColor.getRGB());
        }
    }

    public void reset() {
        taskRunner.reset();
    }
}
