package com.compilerexplorer.project.idea;

import com.compilerexplorer.Pipeline;
import com.intellij.execution.ExecutionTarget;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class ExecutionTargetListener implements com.intellij.execution.ExecutionTargetListener {
    @NonNls
    private static final Logger LOG = Logger.getInstance(ExecutionTargetListener.class);

    @NotNull
    private final Project project;

    public ExecutionTargetListener(@NotNull Project project_) {
        LOG.debug("created");
        project = project_;
    }

    @Override
    public void activeTargetChanged(@NotNull ExecutionTarget newTarget) {
        LOG.debug("activeTargetChanged");
        changed(project);
    }

    private void changed(@NotNull Project project) {
        Pipeline pipeline = project.getUserData(Pipeline.KEY);
        if (pipeline != null) {
            pipeline.scheduleRefresh();
        }
    }
}
