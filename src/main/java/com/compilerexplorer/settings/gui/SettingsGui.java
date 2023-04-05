package com.compilerexplorer.settings.gui;

import com.compilerexplorer.common.Bundle;
import com.compilerexplorer.common.TaskRunner;
import com.compilerexplorer.common.TooltipUtil;
import com.compilerexplorer.datamodel.state.SettingsState;
import com.compilerexplorer.explorer.RemoteCompilersProducer;
import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.ui.AncestorListenerAdapter;
import com.intellij.ui.components.AnActionLink;
import com.intellij.ui.components.JBTextField;
import com.intellij.ui.components.panels.VerticalLayout;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import java.awt.*;
import java.util.stream.Collectors;

public class SettingsGui {
    public static final Key<SettingsGui> KEY = Key.create("compilerexplorer.SettingsGui");
    private static final int GAP = 2;

    @NotNull
    private final Project project;
    @NotNull
    private final SettingsState state;
    @NotNull
    private final JPanel content;
    @NotNull
    private final UrlGui urlGui;
    @NotNull
    private final JLabel testResultLabel;
    @NotNull
    private final JCheckBox preprocessCheckbox;
    @NotNull
    private final JTextField delayMillisField;
    @NotNull
    private final JTextField compilerTimeoutMillisField;
    @NotNull
    private final JTextField ignoreSwitchesField;
    @NotNull
    private final TaskRunner taskRunner;

    public SettingsGui(@NotNull Project project_, boolean showUrlHistoryOnStart) {
        project = project_;

        project.putUserData(KEY, this);

        taskRunner = new TaskRunner();
        state = new SettingsState();
        content = new JPanel(new VerticalLayout(GAP));
        JPanel urlPanel = new JPanel(new BorderLayout(GAP, GAP));
        JLabel urlLabel = new JLabel();
        urlLabel.setVisible(true);
        urlLabel.setText(Bundle.format("compilerexplorer.SettingsGui.UrlLabel"));
        urlPanel.add(urlLabel, BorderLayout.WEST);

        urlGui = new UrlGui();
        urlPanel.add(urlGui.getComponent(), BorderLayout.CENTER);

        AnAction testConnectionAction = ActionManager.getInstance().getAction("compilerexplorer.TestConnection");
        JButton connectButton = new JButton();
        connectButton.setText(Bundle.get("compilerexplorer.SettingsGui.TestConnectionButton"));
        connectButton.setText(testConnectionAction.getTemplatePresentation().getText());
        connectButton.setToolTipText(TooltipUtil.prettify(testConnectionAction.getTemplatePresentation().getDescription()));
        connectButton.setIcon(testConnectionAction.getTemplatePresentation().getIcon());
        connectButton.addActionListener(e -> testConnectionAction.actionPerformed(AnActionEvent.createFromAnAction(testConnectionAction, null, ActionPlaces.UNKNOWN, DataManager.getInstance().getDataContext(connectButton))));
        urlPanel.add(connectButton, BorderLayout.EAST);

        content.add(urlPanel, VerticalLayout.TOP);

        JPanel testResultPanel = new JPanel(new BorderLayout(GAP, GAP));
        testResultLabel = new JLabel();
        testResultLabel.setVisible(true);
        testResultLabel.setText("");
        testResultPanel.add(testResultLabel, BorderLayout.CENTER);

        content.add(testResultPanel, VerticalLayout.TOP);

        JPanel ignoreSwitchesPanel = new JPanel(new BorderLayout(GAP, GAP));
        JLabel ignoreSwitchesLabel = new JLabel();
        ignoreSwitchesLabel.setVisible(true);
        ignoreSwitchesLabel.setText(Bundle.get("compilerexplorer.SettingsGui.IgnoreSwitchesLabel"));
        ignoreSwitchesPanel.add(ignoreSwitchesLabel, BorderLayout.WEST);
        ignoreSwitchesField = new JBTextField(20);
        ignoreSwitchesPanel.add(ignoreSwitchesField, BorderLayout.CENTER);

        content.add(ignoreSwitchesPanel, VerticalLayout.TOP);

        JPanel preprocessPanel = new JPanel(new BorderLayout(GAP, GAP));
        preprocessCheckbox = new JCheckBox();
        preprocessCheckbox.setText(Bundle.get("compilerexplorer.SettingsGui.PreprocessLocallyLabel"));
        preprocessPanel.add(preprocessCheckbox, BorderLayout.WEST);

        content.add(preprocessPanel, VerticalLayout.TOP);

        JPanel delayMillisPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, GAP, GAP));
        JLabel delayMillisLabel = new JLabel();
        delayMillisLabel.setVisible(true);
        delayMillisLabel.setText(Bundle.get("compilerexplorer.SettingsGui.AutoupdateDelayLabel"));
        delayMillisPanel.add(delayMillisLabel);
        delayMillisField = new JBTextField(6);
        delayMillisPanel.add(delayMillisField);

        content.add(delayMillisPanel, VerticalLayout.TOP);

        JPanel compilerTimeoutMillisPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, GAP, GAP));
        JLabel compilerTimeoutMillisLabel = new JLabel();
        compilerTimeoutMillisLabel.setVisible(true);
        compilerTimeoutMillisLabel.setText(Bundle.get("compilerexplorer.SettingsGui.CompilerTimeoutLabel"));
        compilerTimeoutMillisPanel.add(compilerTimeoutMillisLabel);
        compilerTimeoutMillisField = new JBTextField(6);
        compilerTimeoutMillisPanel.add(compilerTimeoutMillisField);

        content.add(compilerTimeoutMillisPanel, VerticalLayout.TOP);

        JPanel colorsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, GAP, GAP));
        AnAction colorsAction = ActionManager.getInstance().getAction("compilerexplorer.ShowColorSettings");
        AnActionLink colorsLink = new AnActionLink(colorsAction.getTemplateText() != null ? colorsAction.getTemplateText() : "", colorsAction);
        colorsPanel.add(colorsLink);

        content.add(colorsPanel, VerticalLayout.TOP);

        content.addAncestorListener(new AncestorListenerAdapter() {
            @Override
            public void ancestorAdded(AncestorEvent event) {
                if (showUrlHistoryOnStart) {
                    ApplicationManager.getApplication().invokeLater(urlGui::showUrlHistory);
                }
            }
        });
    }

    public void copyFrom(@NotNull SettingsState state_) {
        state.copyFrom(state_);
        populateGuiFromState();
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
        urlGui.setUrl(state.getUrl());
        refreshUrlHistoryInGui();

        preprocessCheckbox.setSelected(state.getPreprocessLocally());
        delayMillisField.setText(String.valueOf(state.getDelayMillis()));
        compilerTimeoutMillisField.setText(String.valueOf(state.getCompilerTimeoutMillis()));
        ignoreSwitchesField.setText(state.getIgnoreSwitches());
    }

    private void populateStateFromGui(@NotNull SettingsState state_) {
        String currentUrl = urlGui.getUrl();
        if (!state_.getUrl().equals(currentUrl)) {
            state_.setConnected(false);
        }
        state_.setUrl(currentUrl);
        state_.setUrlHistory(urlGui.getUrlHistory());
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

    public void dispose() {
        taskRunner.reset();

        project.putUserData(KEY, null);
    }

    public static boolean isSettingsGuiActive(@NotNull Project project) {
        return project.getUserData(KEY) != null;
    }

    public void testConnection() {
        SettingsState testState = new SettingsState();
        populateStateFromGui(testState);
        testState.setConnected(false);
        (new RemoteCompilersProducer(
                project,
                testState,
                url -> {
                    testResultLabel.setText(Bundle.format("compilerexplorer.SettingsGui.TestConnectionSuccess", "NumberOfCompilers", Integer.toString(testState.getRemoteCompilers().size())));
                    testResultLabel.setToolTipText(testState.getRemoteCompilers().stream().map(c -> c.getLanguage() + " " + c.getName()).collect(Collectors.joining(TooltipUtil.HTML_LINE_BREAK)));
                    state.addToUrlHistory(url);
                    refreshUrlHistoryInGui();
                },
                taskRunner
        )).testConnection(exception -> {
            testResultLabel.setText(Bundle.format("compilerexplorer.SettingsGui.TestConnectionError", "Exception", exception.getMessage()));
            testResultLabel.setToolTipText("");
        });
    }

    public void refreshUrlHistoryInGui() {
        urlGui.setUrlHistory(state.getUrlHistory());
    }
}
