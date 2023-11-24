package com.compilerexplorer.project.oc;

import com.compilerexplorer.Pipeline;
import com.compilerexplorer.project.PipelineNotifierOnProjectChange;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.jetbrains.cidr.project.workspace.CidrWorkspace;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;


public class CidrWorkspaceListener extends PipelineNotifierOnProjectChange implements com.jetbrains.cidr.project.workspace.CidrWorkspaceListener {
    @NonNls
    private static final Logger LOG = Logger.getInstance(CidrWorkspaceListener.class);

    @NotNull
    private final Project project;

    public CidrWorkspaceListener(@NotNull Project project_) {
        super(Pipeline::workspaceInitialized, Pipeline.WORKSPACE_INITIALIZED_KEY);
        LOG.debug("created");
        project = project_;
    }

    @Override
    public void initialized(@NotNull CidrWorkspace workspace) {
        LOG.debug("initialized");
        changed(project);
    }
}
