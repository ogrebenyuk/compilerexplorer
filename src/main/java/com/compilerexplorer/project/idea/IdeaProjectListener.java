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

public class IdeaProjectListener {
    public IdeaProjectListener(@NotNull Project project, @NotNull Runnable changeConsumer) {
        project.getMessageBus().connect().subscribe(RunManagerListener.TOPIC, new RunManagerListener() {
            public void runConfigurationAdded(@NotNull RunnerAndConfigurationSettings conf) {
                changeConsumer.run();
            }
            public void runConfigurationRemoved(@NotNull RunnerAndConfigurationSettings conf) {
                changeConsumer.run();
            }
            public void runConfigurationChanged(@NotNull RunnerAndConfigurationSettings conf) {
                changeConsumer.run();
            }
        });
        project.getMessageBus().connect().subscribe(ExecutionTargetManager.TOPIC, e -> changeConsumer.run());
        project.getMessageBus().connect().subscribe(ProjectTopics.MODULES, new ModuleListener() {
            @Deprecated
            @Override
            public void moduleAdded(@NotNull Project project, @NotNull Module module) {
                changeConsumer.run();
            }
            @Override
            public void beforeModuleRemoved(@NotNull Project project, @NotNull Module module) {
                changeConsumer.run();
            }
            @Override
            public void moduleRemoved(@NotNull Project project, @NotNull Module module) {
                changeConsumer.run();
            }
            @Override
            public void modulesRenamed(@NotNull Project project, @NotNull List<? extends Module> modules, @NotNull Function<? super Module, String> oldNameProvider) {
                changeConsumer.run();
            }
        });
        project.getMessageBus().connect().subscribe(ProjectTopics.PROJECT_ROOTS, new ModuleRootListener() {
            @Override
            public void rootsChanged(@NotNull ModuleRootEvent event) {
                changeConsumer.run();
            }
        });
    }
}
