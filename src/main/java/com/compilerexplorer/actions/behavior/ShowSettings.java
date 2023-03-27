package com.compilerexplorer.actions.behavior;

import com.compilerexplorer.actions.common.BaseAction;
import com.compilerexplorer.common.Constants;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.options.ShowSettingsUtil;
import org.jetbrains.annotations.NotNull;

public class ShowSettings extends BaseAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        withProject(event, project -> ShowSettingsUtil.getInstance().showSettingsDialog(project, Constants.PROJECT_TITLE));
    }
}
