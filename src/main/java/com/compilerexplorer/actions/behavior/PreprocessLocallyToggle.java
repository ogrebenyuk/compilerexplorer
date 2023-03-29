package com.compilerexplorer.actions.behavior;

import com.compilerexplorer.actions.common.StateToggleAction;
import com.compilerexplorer.datamodel.state.SettingsState;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class PreprocessLocallyToggle extends StateToggleAction {
    @Override
    public boolean isSelected(@NotNull AnActionEvent event) {
        return isSelected(event, SettingsState::getPreprocessLocally);
    }

    @Override
    public void setSelected(@NotNull AnActionEvent event, boolean selected) {
        setSelected(event, selected, SettingsState::setPreprocessLocally);
    }
}
