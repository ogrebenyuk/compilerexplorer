package com.compilerexplorer.actions.appearance.tabs;

import com.compilerexplorer.actions.common.BaseAction;
import com.compilerexplorer.actions.common.BaseActionWithEditorGui;
import com.compilerexplorer.actions.common.BaseActionWithFilters;
import com.compilerexplorer.common.Constants;
import com.compilerexplorer.common.Tabs;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

class BaseShowTab extends BaseAction implements BaseActionWithFilters, BaseActionWithEditorGui {
    @NotNull
    private final Tabs tab;

    public BaseShowTab(@NotNull Tabs tab_) {
        tab = tab_;
    }

    @Override
    public void update(@NotNull final AnActionEvent event) {
        super.update(event);
        withEditorGui(event, editorGui -> {
            setEnabled(event, isEnabled(event) && editorGui.isTabEnabled(tab));
            setIcon(event, editorGui.isTabError(tab) ? Constants.TAB_ERROR_ICON : Constants.TAB_NO_ERROR_ERROR_ICON);
        });
    }

    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        withEditorGui(event, editorGui -> editorGui.showTab(tab));
    }
}
