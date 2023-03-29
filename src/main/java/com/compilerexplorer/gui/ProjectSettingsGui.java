package com.compilerexplorer.gui;

import com.compilerexplorer.common.LaterConsumerUnlessSuppressed;
import com.compilerexplorer.common.RefreshSignal;
import com.compilerexplorer.common.SuppressionFlag;
import com.compilerexplorer.datamodel.ProjectSettings;
import com.compilerexplorer.datamodel.SelectedSourceSettings;
import com.compilerexplorer.datamodel.SourceSettings;
import com.compilerexplorer.gui.listeners.ComboBoxSelectionListener;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.SimpleListCellRenderer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemListener;
import java.util.Objects;
import java.util.function.Consumer;

public class ProjectSettingsGui implements Consumer<ProjectSettings> {
    @NotNull
    private final ComboBox<SourceSettings> comboBox;
    @NotNull
    private final SuppressionFlag suppressionFlag;
    @NotNull
    private final Consumer<SelectedSourceSettings> sourceSettingsConsumer;
    @Nullable private ItemListener selectionListener;

    public ProjectSettingsGui(@NotNull SuppressionFlag suppressionFlag_, @NotNull Consumer<SelectedSourceSettings> sourceSettingsConsumer_) {
        suppressionFlag = suppressionFlag_;
        sourceSettingsConsumer = sourceSettingsConsumer_;

        comboBox = new ComboBox<>();
        comboBox.setRenderer(new SimpleListCellRenderer<>() {
            @Override
            public void customize(@Nullable JList list, @Nullable SourceSettings value, int index, boolean isSelected, boolean cellHasFocus) {
                setText((value != null) ? getText(value) : "");
            }
            @NotNull
            private String getText(@NotNull SourceSettings value) {
                return value.sourceName;
            }
        });
    }

    @NotNull
    public Component getComponent() {
        return comboBox;
    }

    @NotNull
    public Consumer<RefreshSignal> asResetSignalConsumer() {
        return refreshSignal -> {
            ApplicationManager.getApplication().assertIsDispatchThread();
            clear();
        };
    }

    @Override
    public void accept(@NotNull ProjectSettings projectSettings) {
        suppressionFlag.apply(() -> projectSettingsChanged(projectSettings));
    }

    private void projectSettingsChanged(@NotNull ProjectSettings projectSettings) {
        ApplicationManager.getApplication().assertIsDispatchThread();

        if (selectionListener != null) {
            comboBox.removeItemListener(selectionListener);
        }
        selectionListener = new ComboBoxSelectionListener<>(comboBox,
                new LaterConsumerUnlessSuppressed<>(selectedSource -> sourceSettingsChanged(projectSettings, selectedSource), suppressionFlag)
        );
        comboBox.addItemListener(selectionListener);


        SourceSettings oldSelection = comboBox.getItemAt(comboBox.getSelectedIndex());
        SourceSettings newSelection = projectSettings.getSettings().stream()
                .filter(x -> oldSelection != null && Objects.equals(x.sourcePath, oldSelection.sourcePath))
                .findFirst()
                .orElse(projectSettings.getSettings().size() != 0 ? projectSettings.getSettings().get(0) : null);
        DefaultComboBoxModel<SourceSettings> model = new DefaultComboBoxModel<>(projectSettings.getSettings().toArray(new SourceSettings[0]));
        model.setSelectedItem(newSelection);
        comboBox.setModel(model);
        sourceSettingsChanged(projectSettings, newSelection);
    }

    private void sourceSettingsChanged(@NotNull ProjectSettings projectSettings, @Nullable SourceSettings sourceSettings) {
        SelectedSourceSettings selectedSource = new SelectedSourceSettings(projectSettings);
        if (sourceSettings != null) {
            selectedSource.selectedSourceSettings = sourceSettings;
            comboBox.setToolTipText(getSourceTooltip(sourceSettings));
        } else {
            clear();
        }
        sourceSettingsConsumer.accept(selectedSource);
    }

    @NotNull
    private String getSourceTooltip(@NotNull SourceSettings sourceSettings) {
        return "File: " + sourceSettings.sourcePath
                + "<br/>Language: " + sourceSettings.language
                + "<br/>Compiler: " + sourceSettings.compilerPath
                + "<br/>Compiler kind: " + sourceSettings.compilerKind
                + "<br/>Compiler options: " + String.join(" ", sourceSettings.switches);
    }

    private void clear() {
        comboBox.removeAllItems();
        comboBox.setToolTipText("");
    }
}
