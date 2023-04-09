package com.compilerexplorer.actions.extra;

import com.compilerexplorer.Pipeline;
import com.compilerexplorer.actions.common.BaseAction;
import com.compilerexplorer.actions.common.BaseActionWithPipeline;
import com.compilerexplorer.actions.common.BaseActionWithProject;
import com.compilerexplorer.gui.EnabledRemoteLibrariesGui;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class EnabledRemoteLibraries extends BaseAction implements BaseActionWithProject, BaseActionWithPipeline {
    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        withProject(event, project -> {
            if (EnabledRemoteLibrariesGui.show(project)) {
                withPipeline(event, Pipeline::preprocess);
            }
        });
    }
}
