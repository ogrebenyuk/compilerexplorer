package com.compilerexplorer.project;

import com.compilerexplorer.common.SettingsProvider;
import com.compilerexplorer.common.datamodel.ProjectSettings;
import com.compilerexplorer.project.clion.oc.OCProjectListener;
import com.compilerexplorer.project.clion.oc.OCProjectSettingsProducer;
import com.compilerexplorer.project.idea.IdeaProjectListener;
import com.intellij.openapi.project.Project;
import com.intellij.util.Producer;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class ProjectListener {
    @NotNull
    private final Project project;
    @NotNull
    private final Consumer<ProjectSettings> projectSettingsConsumer;
    @NotNull
    private final Producer<ProjectSettings> ocProjectSettingsProducer;

    public ProjectListener(@NotNull Project project_, @NotNull Consumer<ProjectSettings> projectSettingsConsumer_) {
        project = project_;
        projectSettingsConsumer = projectSettingsConsumer_;
        ocProjectSettingsProducer = new OCProjectSettingsProducer(project);
        new IdeaProjectListener(project, this::changed);
        new OCProjectListener(project, this::changed);
    }

    private void changed(Boolean unused) {
        System.out.println("ProjectListener::changed");
        refresh();
    }

    public void refresh() {
        if (!SettingsProvider.getInstance(project).getState().getEnabled()) {
            return;
        }

        System.out.println("ProjectListener::refresh");
        projectSettingsConsumer.accept(ocProjectSettingsProducer.produce());
    }
}
