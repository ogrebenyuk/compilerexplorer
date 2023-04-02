package com.compilerexplorer.actions.appearance;

import com.compilerexplorer.actions.common.BaseActionWithEditorGui;
import com.compilerexplorer.actions.common.BaseActionWithFilters;
import com.compilerexplorer.actions.common.BaseAppearanceToggleAction;
import com.compilerexplorer.datamodel.state.SettingsState;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class ShowOpcodesToggle extends BaseAppearanceToggleAction implements BaseActionWithFilters, BaseActionWithEditorGui {
    @Override
    public boolean isSelected(@NotNull AnActionEvent event) {
        return isSelected(event, SettingsState::getShowOpcodes);
    }

    @Override
    public void setSelected(@NotNull AnActionEvent event, boolean selected) {
        setSelected(event, selected, SettingsState::setShowOpcodes, withEditorGuiRun(event, editorGui -> editorGui.refresh(false)));
    }

    @Override
    public void update(@NotNull final AnActionEvent event) {
        super.update(event);
        withFilters(event, filters -> setEnabled(event, isEnabled(event) && filters.isAnyBinaryRequested()));
    }
}
