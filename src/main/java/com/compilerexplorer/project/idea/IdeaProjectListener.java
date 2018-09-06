package com.compilerexplorer.project.idea;

import com.compilerexplorer.project.common.ProjectSettingsProducer;
import com.intellij.ProjectTopics;
import com.intellij.execution.*;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.ModuleListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootEvent;
import com.intellij.openapi.roots.ModuleRootListener;
import com.intellij.util.Function;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class IdeaProjectListener {
    @NotNull
    private final Project project;
    @NotNull
    private final ProjectSettingsProducer projectSettingsProducer;

    public IdeaProjectListener(@NotNull Project project_, @NotNull ProjectSettingsProducer projectSettingsProducer_) {
        project = project_;
        projectSettingsProducer = projectSettingsProducer_;
        subscribeToProjectChanges();
    }

    private void subscribeToProjectChanges() {
        project.getMessageBus().connect().subscribe(RunManagerListener.TOPIC, new RunManagerListener() {
            public void runConfigurationSelected() {
                changed();
            }
            public void runConfigurationAdded(@NotNull RunnerAndConfigurationSettings conf) {
                changed();
            }
            public void runConfigurationRemoved(@NotNull RunnerAndConfigurationSettings conf) {
                changed();
            }
            public void runConfigurationChanged(@NotNull RunnerAndConfigurationSettings conf) {
                changed();
            }
        });
        project.getMessageBus().connect().subscribe(ExecutionTargetManager.TOPIC, e -> this.changed());
        project.getMessageBus().connect().subscribe(ProjectTopics.MODULES, new ModuleListener() {
            @Override
            public void moduleAdded(@NotNull Project project, @NotNull Module module) {
                changed();
            }
            @Override
            public void beforeModuleRemoved(@NotNull Project project, @NotNull Module module) {
                changed();
            }
            @Override
            public void moduleRemoved(@NotNull Project project, @NotNull Module module) {
                changed();
            }
            @Override
            public void modulesRenamed(@NotNull Project project, @NotNull List<Module> modules, @NotNull Function<Module, String> oldNameProvider) {
                changed();
            }
        });
        project.getMessageBus().connect().subscribe(ProjectTopics.PROJECT_ROOTS, new ModuleRootListener() {
            @Override
            public void rootsChanged(ModuleRootEvent event) {
                changed();
            }
        });
    }

    private void changed() {
        projectSettingsProducer.projectChanged();
    }
}
