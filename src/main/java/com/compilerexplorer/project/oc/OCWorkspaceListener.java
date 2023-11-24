package com.compilerexplorer.project.oc;

import com.compilerexplorer.Pipeline;
import com.compilerexplorer.project.PipelineNotifierOnProjectChange;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;


public class OCWorkspaceListener extends PipelineNotifierOnProjectChange implements com.jetbrains.cidr.lang.workspace.OCWorkspaceListener {
    @NonNls
    private static final Logger LOG = Logger.getInstance(OCWorkspaceListener.class);

    @NotNull
    private final Project project;

    public OCWorkspaceListener(@NotNull Project project_) {
        super(Pipeline::scheduleRefresh);
        LOG.debug("created");
        project = project_;
    }

    @Override
    public void workspaceChanged(@NotNull com.jetbrains.cidr.lang.workspace.OCWorkspaceListener.OCWorkspaceEvent event) {
        LOG.debug("workspaceChanged");
        changed(project);
    }

    @Override
    public void selectedResolveConfigurationChanged() {
        LOG.debug("selectedResolveConfigurationChanged");
        changed(project);
    }
}
