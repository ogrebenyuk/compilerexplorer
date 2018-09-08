package com.compilerexplorer.project;

import com.compilerexplorer.common.datamodel.ProjectSettingsConsumer;
import com.compilerexplorer.project.clion.oc.OCProjectListener;
import com.compilerexplorer.project.clion.oc.OCProjectSettingsProducer;
import com.compilerexplorer.project.idea.IdeaProjectListener;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class ProjectListener {
    @NotNull
    private final OCProjectSettingsProducer ocProjectSettingsProducer;

    public ProjectListener(@NotNull Project project, @NotNull ProjectSettingsConsumer projectSettingsConsumer) {
        ocProjectSettingsProducer = new OCProjectSettingsProducer(project, projectSettingsConsumer);
        new IdeaProjectListener(project, ocProjectSettingsProducer);
        new OCProjectListener(project, ocProjectSettingsProducer);
    }

    public void refresh() {
        ocProjectSettingsProducer.projectChanged();
    }
}
