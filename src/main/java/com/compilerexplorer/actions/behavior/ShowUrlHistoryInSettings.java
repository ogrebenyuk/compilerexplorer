package com.compilerexplorer.actions.behavior;

import com.compilerexplorer.actions.common.BaseAction;
import com.compilerexplorer.settings.CompilerExplorerSettingsConfigurable;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.options.ShowSettingsUtil;
import org.jetbrains.annotations.NotNull;

public class ShowUrlHistoryInSettings extends BaseAction {
    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        withProject(event, project -> ShowSettingsUtil.getInstance().showSettingsDialog(project, CompilerExplorerSettingsConfigurable.class,
                configurable -> configurable.showUrlHistoryOnStart(true)
        ));
    }
}
