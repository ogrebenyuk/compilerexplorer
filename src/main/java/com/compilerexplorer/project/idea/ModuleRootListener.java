package com.compilerexplorer.project.idea;

import com.compilerexplorer.Pipeline;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootEvent;
import org.jetbrains.annotations.NotNull;

public class ModuleRootListener implements com.intellij.openapi.roots.ModuleRootListener {
    private static final Logger LOG = Logger.getInstance(ModuleRootListener.class);

    @NotNull
    private final Project project;

    public ModuleRootListener(@NotNull Project project_) {
        LOG.debug("created");
        project = project_;
    }

    @Override
    public void rootsChanged(@NotNull ModuleRootEvent event) {
        LOG.debug("rootsChanged");
        changed(project);
    }

    private void changed(@NotNull Project project) {
        Pipeline pipeline = project.getUserData(Pipeline.KEY);
        if (pipeline != null) {
            pipeline.scheduleRefresh();
        }
    }
}
