package com.compilerexplorer.actions.filter;

import com.compilerexplorer.actions.common.FiltersToggleAction;
import com.compilerexplorer.datamodel.state.Filters;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class UnusedLabelsToggle extends FiltersToggleAction {
    @Override
    public boolean isSelected(@NotNull AnActionEvent event) {
        return isSelected(event, Filters::getLabels);
    }

    @Override
    public void setSelected(@NotNull AnActionEvent event, boolean selected) {
        setSelected(event, selected, Filters::setLabels);
    }
}
