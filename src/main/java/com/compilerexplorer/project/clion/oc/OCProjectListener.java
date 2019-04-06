package com.compilerexplorer.project.clion.oc;

import com.intellij.openapi.project.Project;
import com.jetbrains.cidr.lang.workspace.OCWorkspaceListener;
import org.jetbrains.annotations.NotNull;

public class OCProjectListener {
    public OCProjectListener(@NotNull Project project, @NotNull Runnable changeConsumer) {
        project.getMessageBus().connect().subscribe(OCWorkspaceListener.TOPIC, new OCWorkspaceListener() {
            public void workspaceChanged(@NotNull OCWorkspaceListener.OCWorkspaceEvent event) {
                changeConsumer.run();
            }
            public void selectedResolveConfigurationChanged() {
                changeConsumer.run();
            }
        });
    }
}
