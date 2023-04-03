package com.compilerexplorer.actions.behavior;

import com.compilerexplorer.actions.common.BaseAction;
import com.compilerexplorer.actions.common.BaseActionWithEditorGui;
import com.compilerexplorer.gui.EditorGui;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class SaveCurrentTabAs extends BaseAction implements BaseActionWithEditorGui {
    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        withEditorGui(event, EditorGui::saveCurrentTabAs);
    }
}
