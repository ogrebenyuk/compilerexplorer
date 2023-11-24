package com.compilerexplorer.project.bazel;

import com.compilerexplorer.Pipeline;
import com.compilerexplorer.project.PipelineNotifierOnProjectChange;
import com.google.common.collect.ImmutableSet;
import com.google.idea.blaze.base.scope.BlazeContext;
import com.google.idea.blaze.base.sync.SyncListener;
import com.google.idea.blaze.base.sync.SyncMode;
import com.google.idea.blaze.base.sync.SyncResult;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NonNls;

public class BazelInitializationDetector extends PipelineNotifierOnProjectChange implements SyncListener {
    @NonNls
    private static final Logger LOG = Logger.getInstance(BazelInitializationDetector.class);

    private boolean triggered = false;

    public BazelInitializationDetector() {
        super(Pipeline::workspaceInitialized, Pipeline.WORKSPACE_INITIALIZED_KEY);
        LOG.debug("created");
    }

    public void afterSync(Project project, BlazeContext context, SyncMode syncMode, SyncResult syncResult, ImmutableSet<Integer> buildIds) {
        LOG.debug("afterSync");
        if (syncMode != SyncMode.NO_BUILD && !triggered) {
            triggered = true;
            changed(project);
        }
    }
}
