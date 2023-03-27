package com.compilerexplorer.actions.behavior;

import com.compilerexplorer.actions.common.BaseAction;
import com.compilerexplorer.actions.common.BaseActionWithToolWindowGui;
import com.compilerexplorer.gui.ToolWindowGui;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class RecompileCurrentSource extends BaseAction implements BaseActionWithToolWindowGui {
    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        withToolWindowGui(event, ToolWindowGui::preprocess);
    }
}
