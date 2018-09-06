package com.compilerexplorer.project.clion.oc;

import com.compilerexplorer.project.common.ProjectSettingsProducer;
import com.intellij.openapi.project.Project;
import com.jetbrains.cidr.lang.workspace.OCWorkspaceModificationListener;
import org.jetbrains.annotations.NotNull;

public class OCProjectListener {
    @NotNull
    private final Project project;
    @NotNull
    private final ProjectSettingsProducer projectSettingsProducer;

    public OCProjectListener(@NotNull Project project_, @NotNull ProjectSettingsProducer projectSettingsProducer_) {
        project = project_;
        projectSettingsProducer = projectSettingsProducer_;
        subscribeToProjectChanges();
    }

    private void subscribeToProjectChanges() {
        project.getMessageBus().connect().subscribe(OCWorkspaceModificationListener.TOPIC, new OCWorkspaceModificationListener() {
            public void projectsChanged() {
                changed();
            }
            public void projectFilesChanged() {
                changed();
            }
            public void sourceFilesChanged() {
                changed();
            }
            public void buildSettingsChanged() {
                changed();
            }
            public void selectedResolveConfigurationChanged() {
                changed();
            }
            public void buildFinished() {
                changed();
            }
        });
    }

    private void changed() {
        projectSettingsProducer.projectChanged();
    }
}
