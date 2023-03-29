package com.compilerexplorer.actions.output;

import com.compilerexplorer.actions.common.BaseActionWithEditorGui;
import com.compilerexplorer.actions.common.FiltersToggleAction;
import com.compilerexplorer.datamodel.state.Filters;
import com.compilerexplorer.common.Tabs;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class ExecuteCodeToggle extends FiltersToggleAction implements BaseActionWithEditorGui {
    @Override
    public boolean isSelected(@NotNull AnActionEvent event) {
        return isSelected(event, Filters::getExecute);
    }

    @Override
    public void setSelected(@NotNull AnActionEvent event, boolean selected) {
        setSelected(event, selected, Filters::setExecute);
        withEditorGui(event, editorGui -> {
            if (selected) {
                editorGui.requestTab(Tabs.EXPLORER_EXEC_RESULT);
            }
        });
    }
}
