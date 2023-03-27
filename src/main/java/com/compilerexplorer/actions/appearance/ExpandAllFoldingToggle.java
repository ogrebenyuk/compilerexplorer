package com.compilerexplorer.actions.appearance;

import com.compilerexplorer.actions.common.BaseAction;
import com.compilerexplorer.actions.common.BaseActionWithEditorGui;
import com.compilerexplorer.actions.common.BaseActionWithState;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class ExpandAllFoldingToggle extends BaseAction implements BaseActionWithEditorGui, BaseActionWithState {
    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        withEditorGui(event, editorGui -> editorGui.expandAllFolding(true));
    }

    @Override
    public void update(@NotNull final AnActionEvent event) {
        super.update(event);
        withState(event, state -> setEnabled(event, isEnabled(event) && state.getEnableFolding()));
    }
}
