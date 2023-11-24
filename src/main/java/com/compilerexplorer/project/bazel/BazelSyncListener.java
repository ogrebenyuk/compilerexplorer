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

public class BazelSyncListener extends PipelineNotifierOnProjectChange implements SyncListener {
    @NonNls
    private static final Logger LOG = Logger.getInstance(BazelSyncListener.class);

    public BazelSyncListener() {
        super(Pipeline::scheduleRefresh);
        LOG.debug("created");
    }

    public void afterSync(Project project, BlazeContext context, SyncMode syncMode, SyncResult syncResult, ImmutableSet<Integer> buildIds) {
        LOG.debug("afterSync");
        if (syncMode != SyncMode.NO_BUILD) {
            changed(project);
        }
    }
}
