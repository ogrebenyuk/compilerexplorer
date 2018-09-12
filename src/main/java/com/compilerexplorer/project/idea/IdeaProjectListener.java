package com.compilerexplorer.project.idea;

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
import java.util.function.Consumer;

public class IdeaProjectListener {
    public IdeaProjectListener(@NotNull Project project, @NotNull Consumer<Boolean> changeConsumer) {
        project.getMessageBus().connect().subscribe(RunManagerListener.TOPIC, new RunManagerListener() {
            public void runConfigurationSelected() {
                changeConsumer.accept(false);
            }
            public void runConfigurationAdded(@NotNull RunnerAndConfigurationSettings conf) {
                changeConsumer.accept(false);
            }
            public void runConfigurationRemoved(@NotNull RunnerAndConfigurationSettings conf) {
                changeConsumer.accept(false);
            }
            public void runConfigurationChanged(@NotNull RunnerAndConfigurationSettings conf) {
                changeConsumer.accept(false);
            }
        });
        project.getMessageBus().connect().subscribe(ExecutionTargetManager.TOPIC, e -> changeConsumer.accept(false));
        project.getMessageBus().connect().subscribe(ProjectTopics.MODULES, new ModuleListener() {
            @Override
            public void moduleAdded(@NotNull Project project, @NotNull Module module) {
                changeConsumer.accept(false);
            }
            @Override
            public void beforeModuleRemoved(@NotNull Project project, @NotNull Module module) {
                changeConsumer.accept(false);
            }
            @Override
            public void moduleRemoved(@NotNull Project project, @NotNull Module module) {
                changeConsumer.accept(false);
            }
            @Override
            public void modulesRenamed(@NotNull Project project, @NotNull List<Module> modules, @NotNull Function<Module, String> oldNameProvider) {
                changeConsumer.accept(false);
            }
        });
        project.getMessageBus().connect().subscribe(ProjectTopics.PROJECT_ROOTS, new ModuleRootListener() {
            @Override
            public void rootsChanged(ModuleRootEvent event) {
                changeConsumer.accept(false);
            }
        });
    }
}
