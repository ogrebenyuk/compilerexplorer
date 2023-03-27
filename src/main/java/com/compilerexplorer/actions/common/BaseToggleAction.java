package com.compilerexplorer.actions.common;

import com.intellij.openapi.actionSystem.ActionUpdateThread;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.ToggleAction;
import org.jetbrains.annotations.NotNull;

public abstract class BaseToggleAction extends ToggleAction implements BaseActionUtil {
    @Override
    public void update(@NotNull final AnActionEvent event) {
        super.update(event);
        setEnabledAndVisible(event, event.getProject() != null);
    }

    @Override
    public @NotNull ActionUpdateThread getActionUpdateThread() {
        return ActionUpdateThread.BGT;
    }
}
