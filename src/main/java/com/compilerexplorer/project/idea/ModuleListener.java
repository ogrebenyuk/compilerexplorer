package com.compilerexplorer.project.idea;

import com.compilerexplorer.Pipeline;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.util.Function;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ModuleListener implements com.intellij.openapi.project.ModuleListener {
    @NonNls
    private static final Logger LOG = Logger.getInstance(ModuleListener.class);

    public ModuleListener(@SuppressWarnings("unused") @NotNull Project project) {
        LOG.debug("created");
    }

    @Override
    public void modulesAdded(@NotNull Project project, @NotNull List<Module> modules) {
        LOG.debug("modulesAdded");
        changed(project);
    }

    @Override
    public void beforeModuleRemoved(@NotNull Project project, @NotNull Module module) {
        LOG.debug("beforeModuleRemoved");
    }

    @Override
    public void moduleRemoved(@NotNull Project project, @NotNull Module module) {
        LOG.debug("moduleRemoved");
        changed(project);
    }

    @Override
    public void modulesRenamed(@NotNull Project project, @NotNull List<? extends Module> modules, @NotNull Function<? super Module, String> oldNameProvider) {
        LOG.debug("modulesRenamed");
        changed(project);
    }

    private void changed(@NotNull Project project) {
        Pipeline pipeline = project.getUserData(Pipeline.KEY);
        if (pipeline != null) {
            pipeline.scheduleRefresh();
        }
    }
}
