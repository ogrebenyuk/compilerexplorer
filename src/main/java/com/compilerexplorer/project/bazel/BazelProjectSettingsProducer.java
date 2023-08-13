package com.compilerexplorer.project.bazel;

import com.compilerexplorer.datamodel.ProjectSources;
import com.compilerexplorer.project.ProjectSettingsProducer;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;

public class BazelProjectSettingsProducer implements ProjectSettingsProducer {
    @Override
    @NotNull
    public ProjectSources get(@NotNull Project project) {
        return new ProjectSources(Collections.emptyList());
    }
}
