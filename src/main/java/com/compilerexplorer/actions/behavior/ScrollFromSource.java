package com.compilerexplorer.actions.behavior;

import com.compilerexplorer.actions.common.BaseAction;
import com.compilerexplorer.actions.common.BaseActionWithEditorGui;
import com.compilerexplorer.actions.common.BaseActionWithState;
import com.compilerexplorer.gui.EditorGui;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class ScrollFromSource extends BaseAction implements BaseActionWithState, BaseActionWithEditorGui {
    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        withEditorGui(event, EditorGui::scrollFromSource);
    }

    @Override
    public void update(@NotNull final AnActionEvent event) {
        super.update(event);
        withState(event, state -> setVisible(event, isVisible(event) && !state.getAutoscrollFromSource()));
    }
}
