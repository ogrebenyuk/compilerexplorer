package com.compilerexplorer.project;

import com.compilerexplorer.datamodel.ProjectSources;
import com.intellij.openapi.extensions.ExtensionPointName;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public interface ProjectSettingsProducer {
    ExtensionPointName<ProjectSettingsProducer> PROJECT_SETTINGS_PRODUCER_EP = ExtensionPointName.create("com.compilerexplorer.compilerexplorer.projectSettingsProducer");

    @NotNull
    ProjectSources get(@NotNull Project project);
}
