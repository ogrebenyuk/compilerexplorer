package com.compilerexplorer.actions.behavior;

import com.compilerexplorer.actions.common.BaseBehaviorToggleAction;
import com.compilerexplorer.datamodel.state.SettingsState;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class AutoscrollToSourceToggle extends BaseBehaviorToggleAction {
    @Override
    public boolean isSelected(@NotNull AnActionEvent event) {
        return isSelected(event, SettingsState::getAutoscrollToSource);
    }

    @Override
    public void setSelected(@NotNull AnActionEvent event, boolean selected) {
        setSelected(event, selected, SettingsState::setAutoscrollToSource);
    }
}
