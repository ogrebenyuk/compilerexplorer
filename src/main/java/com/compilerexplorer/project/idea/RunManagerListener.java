package com.compilerexplorer.project.idea;

import com.compilerexplorer.Pipeline;
import com.compilerexplorer.project.PipelineNotifierOnProjectChange;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class RunManagerListener extends PipelineNotifierOnProjectChange implements com.intellij.execution.RunManagerListener {
    @NonNls
    private static final Logger LOG = Logger.getInstance(RunManagerListener.class);

    @NotNull
    private final Project project;

    public RunManagerListener(@NotNull Project project_) {
        super(Pipeline::scheduleRefresh);
        LOG.debug("created");
        project = project_;
    }

    @Override
    public void runConfigurationAdded(@NotNull RunnerAndConfigurationSettings conf) {
        LOG.debug("runConfigurationAdded");
        changed(project);
    }

    @Override
    public void runConfigurationRemoved(@NotNull RunnerAndConfigurationSettings conf) {
        LOG.debug("runConfigurationRemoved");
        changed(project);
    }

    @Override
    public void runConfigurationChanged(@NotNull RunnerAndConfigurationSettings conf) {
        LOG.debug("runConfigurationChanged");
        changed(project);
    }

    @Override
    public void runConfigurationSelected(@Nullable RunnerAndConfigurationSettings conf) {
        LOG.debug("runConfigurationSelected");
        changed(project);
    }
}
