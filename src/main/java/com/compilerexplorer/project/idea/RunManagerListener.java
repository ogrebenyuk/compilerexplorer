package com.compilerexplorer.project.idea;

import com.compilerexplorer.Pipeline;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class RunManagerListener implements com.intellij.execution.RunManagerListener {
    private static final Logger LOG = Logger.getInstance(RunManagerListener.class);

    @NotNull
    private final Project project;

    public RunManagerListener(@NotNull Project project_) {
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

    private void changed(@NotNull Project project) {
        Pipeline pipeline = project.getUserData(Pipeline.KEY);
        if (pipeline != null) {
            pipeline.scheduleRefresh();
        }
    }
}
