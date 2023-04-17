package com.compilerexplorer.project.idea;

import com.compilerexplorer.Pipeline;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class StartupDetector implements StartupActivity {
    @NonNls
    private static final Logger LOG = Logger.getInstance(StartupDetector.class);

    @Override
    public void runActivity(@NotNull Project project) {
        LOG.debug("runActivity");
        project.putUserData(Pipeline.STARTED_KEY, true);
        Pipeline pipeline = project.getUserData(Pipeline.KEY);
        if (pipeline != null) {
            pipeline.startupHappened();
        }
    }
}
