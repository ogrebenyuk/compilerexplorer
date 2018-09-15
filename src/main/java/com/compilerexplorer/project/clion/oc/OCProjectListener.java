package com.compilerexplorer.project.clion.oc;

import com.intellij.openapi.project.Project;
import com.jetbrains.cidr.lang.workspace.OCWorkspaceModificationListener;
import org.jetbrains.annotations.NotNull;

public class OCProjectListener {
    public OCProjectListener(@NotNull Project project, @NotNull Runnable changeConsumer) {
        project.getMessageBus().connect().subscribe(OCWorkspaceModificationListener.TOPIC, new OCWorkspaceModificationListener() {
            public void projectsChanged() {
                changeConsumer.run();
            }
            public void projectFilesChanged() {
                changeConsumer.run();
            }
            public void sourceFilesChanged() {
                changeConsumer.run();
            }
            public void buildSettingsChanged() {
                changeConsumer.run();
            }
            public void selectedResolveConfigurationChanged() {
                changeConsumer.run();
            }
            public void buildFinished() {
                changeConsumer.run();
            }
        });
    }
}
