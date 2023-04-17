package com.compilerexplorer.project.idea;

import com.compilerexplorer.Pipeline;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import org.jetbrains.annotations.NotNull;

public class StartupDetector implements StartupActivity {
    @Override
    public void runActivity(@NotNull Project project) {
        Pipeline pipeline = project.getUserData(Pipeline.KEY);
        if (pipeline != null) {
            pipeline.startupHappened();
        }
    }
}
