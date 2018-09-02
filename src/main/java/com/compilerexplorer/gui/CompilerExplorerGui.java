package com.compilerexplorer.gui;

import com.compilerexplorer.common.*;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.fileTypes.PlainTextFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.ui.Messages;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.ListCellRendererWrapper;
import com.intellij.ui.components.JBList;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.Vector;

public class CompilerExplorerGui implements ProjectSettingsConsumer, MainTextConsumer {
    @NotNull
    private final JPanel content;
    @NotNull
    private final JPanel headPanel;
    @NotNull
    private final ComboBox<SourceSettings> projectSettingsList;
    @NotNull
    private final JPanel mainPanel;
    @NotNull
    private final EditorTextField editor;

    private SourceSettingsConsumer sourceSettingsConsumer;
    private boolean suppressUpdates = false;

    public CompilerExplorerGui(@NotNull Project project) {
        content = new JPanel(new BorderLayout());
        headPanel = new JPanel(new BorderLayout());
        projectSettingsList = new ComboBox<>();
        projectSettingsList.setRenderer(new ListCellRendererWrapper<SourceSettings>() {
            @Override
            public void customize(JList list, SourceSettings value, int index, boolean isSelected, boolean cellHasFocus) {
                setText(value.getSource().getPath());
            }
        });
        headPanel.add(projectSettingsList, BorderLayout.CENTER);
        content.add(headPanel, BorderLayout.NORTH);
        mainPanel = new JPanel(new BorderLayout());
        content.add(mainPanel, BorderLayout.CENTER);
        editor = new EditorTextField(EditorFactory.getInstance().createDocument(""), project, PlainTextFileType.INSTANCE, true, false);
        mainPanel.add(editor, BorderLayout.CENTER);

        projectSettingsList.addItemListener(event -> {
            if (sourceSettingsConsumer != null && !suppressUpdates && event.getStateChange() == ItemEvent.SELECTED) {
                sourceSettingsConsumer.setSourceSetting(projectSettingsList.getItemAt(projectSettingsList.getSelectedIndex()));
            }
        });
    }

    public void setSourceSettingsConsumer(@NotNull SourceSettingsConsumer sourceSettingsConsumer_) {
        sourceSettingsConsumer = sourceSettingsConsumer_;
    }

    @NotNull
    public JComponent getContent() {
        return content;
    }

    @Override
    public void setProjectSetting(@NotNull ProjectSettings projectSettings) {
        suppressUpdates = true;
        SourceSettings oldSelection = projectSettingsList.getItemAt(projectSettingsList.getSelectedIndex());
        SourceSettings newSelection = projectSettings.getSettings().stream()
                .filter(x -> oldSelection != null && x.getSource().getPath().equals(oldSelection.getSource().getPath()))
                .findFirst()
                .orElse(projectSettings.getSettings().size() != 0 ? projectSettings.getSettings().firstElement() : null);
        DefaultComboBoxModel<SourceSettings> model = new DefaultComboBoxModel<>(projectSettings.getSettings());
        model.setSelectedItem(newSelection);
        projectSettingsList.setModel(model);
        notifyNewSelection(oldSelection, newSelection);
        suppressUpdates = false;
    }

    @Override
    public void setMainText(@NotNull String text) {
        editor.setText(text);
    }

    private void notifyNewSelection(SourceSettings oldSelection, SourceSettings newSelection) {
        if (newSelection == null) {
            projectSettingsList.removeAllItems();
            sourceSettingsConsumer.clearSourceSetting();
        } else if (!newSelection.equals(oldSelection)) {
            sourceSettingsConsumer.setSourceSetting(newSelection);
        }
    }
}

