package com.compilerexplorer.project.idea;

import com.compilerexplorer.Pipeline;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.ProjectActivity;
import kotlin.Unit;
import kotlin.coroutines.Continuation;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class StartupDetector implements ProjectActivity {
    @NonNls
    private static final Logger LOG = Logger.getInstance(StartupDetector.class);

    @Override
    @Nullable
    public Object execute(@NotNull Project project, @NotNull Continuation<? super Unit> continuation) {
        LOG.debug("runActivity");
        project.putUserData(Pipeline.STARTED_KEY, true);
        Pipeline pipeline = project.getUserData(Pipeline.KEY);
        if (pipeline != null) {
            pipeline.startupHappened();
        }
        return null;
    }
}
