package com.compilerexplorer.actions.appearance;

import com.compilerexplorer.actions.common.BaseActionWithEditorGui;
import com.compilerexplorer.actions.common.BaseActionWithFilters;
import com.compilerexplorer.actions.common.BaseAppearanceToggleAction;
import com.compilerexplorer.datamodel.state.SettingsState;
import com.compilerexplorer.gui.EditorGui;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class ShowLineNumbersToggle extends BaseAppearanceToggleAction implements BaseActionWithFilters, BaseActionWithEditorGui {
    @Override
    public boolean isSelected(@NotNull AnActionEvent event) {
        return isSelected(event, SettingsState::getShowLineNumbers);
    }

    @Override
    public void setSelected(@NotNull AnActionEvent event, boolean selected) {
        setSelected(event, selected, SettingsState::setShowLineNumbers, withEditorGuiRun(event, EditorGui::updateGutter));
    }

    @Override
    public void update(@NotNull final AnActionEvent event) {
        super.update(event);
        withFilters(event, filters -> setEnabled(event, isEnabled(event) && !filters.isAnyBinaryRequested()));
    }
}
