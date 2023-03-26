package com.compilerexplorer.gui;

import com.compilerexplorer.common.LaterConsumerUnlessSuppressed;
import com.compilerexplorer.common.RefreshSignal;
import com.compilerexplorer.common.SuppressionFlag;
import com.compilerexplorer.datamodel.ProjectSettings;
import com.compilerexplorer.datamodel.SourceSettings;
import com.compilerexplorer.gui.listeners.ComboBoxSelectionListener;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.SimpleListCellRenderer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class ProjectSettingsGui implements Consumer<ProjectSettings> {
    @NotNull
    private final ComboBox<SourceSettings> comboBox;
    @NotNull
    private final SuppressionFlag suppressionFlag;
    @Nullable
    private Consumer<SourceSettings> sourceSettingsConsumer;

    public ProjectSettingsGui(@NotNull SuppressionFlag suppressionFlag_) {
        suppressionFlag = suppressionFlag_;

        comboBox = new ComboBox<>();
        comboBox.setRenderer(new SimpleListCellRenderer<>() {
            @Override
            public void customize(@Nullable JList list, @Nullable SourceSettings value, int index, boolean isSelected, boolean cellHasFocus) {
                setText((value != null) ? getText(value) : "");
            }
            @NotNull
            private String getText(@NotNull SourceSettings value) {
                return value.getSourceName();
            }
        });
        comboBox.addItemListener(new ComboBoxSelectionListener<>(comboBox, new LaterConsumerUnlessSuppressed<>(this::sourceSettingsChanged, suppressionFlag)));
    }

    @NotNull
    public Component getComponent() {
        return comboBox;
    }

    public void setSourceSettingsConsumer(@NotNull Consumer<SourceSettings> sourceSettingsConsumer_) {
        sourceSettingsConsumer = sourceSettingsConsumer_;
    }

    @NotNull
    public Consumer<RefreshSignal> asResetSignalConsumer() {
        return this::refresh;
    }

    @Override
    public void accept(ProjectSettings projectSettings) {
        suppressionFlag.apply(() -> projectSettingsChanged(projectSettings));
    }

    private void refresh(RefreshSignal refreshSignal) {
        ApplicationManager.getApplication().assertIsDispatchThread();
        clear();
    }

    private void projectSettingsChanged(ProjectSettings projectSettings) {
        ApplicationManager.getApplication().assertIsDispatchThread();
        SourceSettings oldSelection = comboBox.getItemAt(comboBox.getSelectedIndex());
        SourceSettings newSelection = projectSettings.getSettings().stream()
                .filter(x -> oldSelection != null && x.getSourcePath().equals(oldSelection.getSourcePath()))
                .findFirst()
                .orElse(projectSettings.getSettings().size() != 0 ? projectSettings.getSettings().firstElement() : null);
        DefaultComboBoxModel<SourceSettings> model = new DefaultComboBoxModel<>(projectSettings.getSettings());
        model.setSelectedItem(newSelection);
        comboBox.setModel(model);
        sourceSettingsChanged(newSelection);
    }

    private void sourceSettingsChanged(@Nullable SourceSettings sourceSettings) {
        if (sourceSettings != null) {
            comboBox.setToolTipText(getSourceTooltip(sourceSettings));
        } else {
            clear();
        }
        if (sourceSettingsConsumer != null) {
            sourceSettingsConsumer.accept(sourceSettings);
        }
    }

    @NotNull
    private String getSourceTooltip(@NotNull SourceSettings sourceSettings) {
        return "File: " + sourceSettings.getSourcePath()
                + "<br/>Language: " + sourceSettings.getLanguage()
                + "<br/>Compiler: " + sourceSettings.getCompilerPath()
                + "<br/>Compiler kind: " + sourceSettings.getCompilerKind()
                + "<br/>Compiler options: " + String.join(" ", sourceSettings.getSwitches());
    }

    private void clear() {
        comboBox.removeAllItems();
        comboBox.setToolTipText("");
    }
}
