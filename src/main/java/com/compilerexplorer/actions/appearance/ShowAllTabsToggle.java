package com.compilerexplorer.actions.appearance;

import com.compilerexplorer.actions.common.BaseActionWithEditorGui;
import com.compilerexplorer.actions.common.BaseAppearanceToggleAction;
import com.compilerexplorer.datamodel.state.SettingsState;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class ShowAllTabsToggle extends BaseAppearanceToggleAction implements BaseActionWithEditorGui {
    @Override
    public boolean isSelected(@NotNull AnActionEvent event) {
        return isSelected(event, SettingsState::getShowAllTabs);
    }

    @Override
    public void setSelected(@NotNull AnActionEvent event, boolean selected) {
        setSelected(event, selected, SettingsState::setShowAllTabs, withEditorGuiRun(event, editorGui -> editorGui.refresh(false)));
    }
}
