package com.compilerexplorer.project.idea;

import com.compilerexplorer.Pipeline;
import com.compilerexplorer.project.PipelineNotifierOnProjectChange;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootEvent;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class ModuleRootListener extends PipelineNotifierOnProjectChange implements com.intellij.openapi.roots.ModuleRootListener {
    @NonNls
    private static final Logger LOG = Logger.getInstance(ModuleRootListener.class);

    @NotNull
    private final Project project;

    public ModuleRootListener(@NotNull Project project_) {
        super(Pipeline::scheduleRefresh);
        LOG.debug("created");
        project = project_;
    }

    @Override
    public void rootsChanged(@NotNull ModuleRootEvent event) {
        LOG.debug("rootsChanged");
        changed(project);
    }
}
