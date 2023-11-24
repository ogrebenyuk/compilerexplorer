package com.compilerexplorer.project.idea;

import com.compilerexplorer.Pipeline;
import com.compilerexplorer.project.PipelineNotifierOnProjectChange;
import com.intellij.execution.ExecutionTarget;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class ExecutionTargetListener extends PipelineNotifierOnProjectChange implements com.intellij.execution.ExecutionTargetListener {
    @NonNls
    private static final Logger LOG = Logger.getInstance(ExecutionTargetListener.class);

    @NotNull
    private final Project project;

    public ExecutionTargetListener(@NotNull Project project_) {
        super(Pipeline::scheduleRefresh);
        LOG.debug("created");
        project = project_;
    }

    @Override
    public void activeTargetChanged(@NotNull ExecutionTarget newTarget) {
        LOG.debug("activeTargetChanged");
        changed(project);
    }
}
