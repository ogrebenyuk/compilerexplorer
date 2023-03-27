package com.compilerexplorer.actions.appearance;

import com.compilerexplorer.actions.common.BaseActionWithEditorGui;
import com.compilerexplorer.actions.common.BaseActionWithState;
import com.compilerexplorer.actions.common.BaseAppearanceToggleAction;
import com.compilerexplorer.datamodel.state.SettingsState;
import com.compilerexplorer.gui.EditorGui;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class ShowSourceAnnotationsToggle extends BaseAppearanceToggleAction implements BaseActionWithState, BaseActionWithEditorGui {
    @Override
    public boolean isSelected(@NotNull AnActionEvent event) {
        return isSelected(event, SettingsState::getShowSourceAnnotations);
    }

    @Override
    public void setSelected(@NotNull AnActionEvent event, boolean selected) {
        setSelected(event, selected, SettingsState::setShowSourceAnnotations, withEditorGuiRun(event, EditorGui::updateGutter));
    }
}
