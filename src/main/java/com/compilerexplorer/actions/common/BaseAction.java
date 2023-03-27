package com.compilerexplorer.actions.common;

import com.intellij.openapi.actionSystem.*;
import org.jetbrains.annotations.NotNull;

public abstract class BaseAction extends AnAction implements BaseActionUtil {
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
