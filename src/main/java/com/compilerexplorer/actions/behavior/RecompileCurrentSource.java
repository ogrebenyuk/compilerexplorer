package com.compilerexplorer.actions.behavior;

import com.compilerexplorer.Pipeline;
import com.compilerexplorer.actions.common.BaseAction;
import com.compilerexplorer.actions.common.BaseActionWithPipeline;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class RecompileCurrentSource extends BaseAction implements BaseActionWithPipeline {
    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        withPipeline(event, Pipeline::preprocess);
    }
}
