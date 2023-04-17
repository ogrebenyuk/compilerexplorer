package com.compilerexplorer.project.oc;

import com.compilerexplorer.Pipeline;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.jetbrains.cidr.project.workspace.CidrWorkspace;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;


public class CidrWorkspaceListener implements com.jetbrains.cidr.project.workspace.CidrWorkspaceListener {
    @NonNls
    private static final Logger LOG = Logger.getInstance(CidrWorkspaceListener.class);

    @NotNull
    private final Project project;

    public CidrWorkspaceListener(@NotNull Project project_) {
        LOG.debug("created");
        project = project_;
    }

    @Override
    public void initialized(@NotNull CidrWorkspace workspace) {
        LOG.debug("initialized");
        project.putUserData(Pipeline.STARTED_KEY, true);
        Pipeline pipeline = project.getUserData(Pipeline.KEY);
        if (pipeline != null) {
            pipeline.workspaceInitialized();
        }
    }
}
