package com.compilerexplorer.settings.gui;

import com.compilerexplorer.common.Constants;
import com.compilerexplorer.common.TaskRunner;
import com.compilerexplorer.datamodel.state.SettingsState;
import com.compilerexplorer.explorer.RemoteCompilersProducer;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.AnActionLink;
import com.intellij.ui.components.JBTextField;
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
    private final JTextField delayMillisField;
    @NotNull
    private final JTextField compilerTimeoutMillisField;
    @NotNull
    private final JTextField ignoreSwitchesField;
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
        urlLabel.setText(Constants.PROJECT_TITLE + " URL: ");
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
                        testResultLabel.setToolTipText(testState.getRemoteCompilers().stream().map(c -> c.getLanguage() + " " + c.getName()).collect(Collectors.joining("<br/>")));
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

        JPanel ignoreSwitchesPanel = new JPanel(new BorderLayout(GAP, GAP));
        JLabel ignoreSwitchesLabel = new JLabel();
        ignoreSwitchesLabel.setVisible(true);
        ignoreSwitchesLabel.setText("Ignore compiler switches: ");
        ignoreSwitchesPanel.add(ignoreSwitchesLabel, BorderLayout.WEST);
        ignoreSwitchesField = new JBTextField(20);
        ignoreSwitchesPanel.add(ignoreSwitchesField, BorderLayout.CENTER);

        content.add(ignoreSwitchesPanel, VerticalLayout.TOP);

        JPanel preprocessPanel = new JPanel(new BorderLayout(GAP, GAP));
        preprocessCheckbox = new JCheckBox();
        preprocessCheckbox.setText("Preprocess locally");
        preprocessPanel.add(preprocessCheckbox, BorderLayout.WEST);

        content.add(preprocessPanel, VerticalLayout.TOP);

        JPanel delayMillisPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, GAP, GAP));
        JLabel delayMillisLabel = new JLabel();
        delayMillisLabel.setVisible(true);
        delayMillisLabel.setText("Autoupdate delay (ms): ");
        delayMillisPanel.add(delayMillisLabel);
        delayMillisField = new JBTextField(6);
        delayMillisPanel.add(delayMillisField);

        content.add(delayMillisPanel, VerticalLayout.TOP);

        JPanel compilerTimeoutMillisPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, GAP, GAP));
        JLabel compilerTimeoutMillisLabel = new JLabel();
        compilerTimeoutMillisLabel.setVisible(true);
        compilerTimeoutMillisLabel.setText("Compiler timeout (ms): ");
        compilerTimeoutMillisPanel.add(compilerTimeoutMillisLabel);
        compilerTimeoutMillisField = new JBTextField(6);
        compilerTimeoutMillisPanel.add(compilerTimeoutMillisField);

        content.add(compilerTimeoutMillisPanel, VerticalLayout.TOP);

        JPanel colorsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, GAP, GAP));
        AnActionLink colorsLink = new AnActionLink(Constants.COLOR_SETTINGS_TITLE, new AnAction() {
            @Override
            public void actionPerformed(@NotNull AnActionEvent event) {
                ShowSettingsUtil.getInstance().showSettingsDialog(event.getProject(), Constants.COLOR_SETTINGS_TITLE);
            }
            @Override
            public void update(@NotNull AnActionEvent event) {
                event.getPresentation().setIcon(AllIcons.General.Settings);
            }
            @Override
            public @NotNull ActionUpdateThread getActionUpdateThread() {
                return ActionUpdateThread.BGT;
            }
        });
        colorsPanel.add(colorsLink);

        content.add(colorsPanel, VerticalLayout.TOP);

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
        delayMillisField.setText(String.valueOf(state.getDelayMillis()));
        compilerTimeoutMillisField.setText(String.valueOf(state.getCompilerTimeoutMillis()));
        ignoreSwitchesField.setText(state.getIgnoreSwitches());
    }

    private void populateStateFromGui(@NotNull SettingsState state_) {
        state_.setUrl(urlField.getText());
        state_.setPreprocessLocally(preprocessCheckbox.isSelected());
        try {
            state_.setDelayMillis(Long.parseLong(delayMillisField.getText()));
        } catch (Exception exception) {
            // empty
        }
        try {
            state_.setCompilerTimeoutMillis(Integer.parseInt(compilerTimeoutMillisField.getText()));
        } catch (Exception exception) {
            // empty
        }
        state_.setIgnoreSwitches(ignoreSwitchesField.getText());
    }

    public void reset() {
        taskRunner.reset();
    }
}
