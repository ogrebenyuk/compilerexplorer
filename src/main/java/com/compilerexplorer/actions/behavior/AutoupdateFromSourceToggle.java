package com.compilerexplorer.actions.behavior;

import com.compilerexplorer.actions.common.BaseBehaviorToggleAction;
import com.compilerexplorer.datamodel.state.SettingsState;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class AutoupdateFromSourceToggle extends BaseBehaviorToggleAction {
    @Override
    public boolean isSelected(@NotNull AnActionEvent event) {
        return isSelected(event, SettingsState::getAutoupdateFromSource);
    }

    @Override
    public void setSelected(@NotNull AnActionEvent event, boolean selected) {
        setSelected(event, selected, SettingsState::setAutoupdateFromSource);
    }
}
