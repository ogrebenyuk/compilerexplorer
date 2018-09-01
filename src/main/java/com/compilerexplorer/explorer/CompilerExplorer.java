package com.compilerexplorer.explorer;

import com.compilerexplorer.base.Explorer;
import com.compilerexplorer.base.TextConsumer;
import com.intellij.ProjectTopics;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.ModuleListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootEvent;
import com.intellij.openapi.roots.ModuleRootListener;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Function;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CompilerExplorer implements Explorer {
    @NotNull
    private final Project project;

    private TextConsumer textConsumer;

    @NotNull
    private String currentText = new String();

    public CompilerExplorer(@NotNull Project project_) {
        project = project_;

        project.getMessageBus().connect().subscribe(ProjectTopics.PROJECT_ROOTS, new ModuleRootListener() {
            @Override
            public void beforeRootsChange(ModuleRootEvent event) {
                String projectName = project.getName();
                addText("beforeRootsChange for " + projectName);
            }

            @Override
            public void rootsChanged(ModuleRootEvent event) {
                String projectName = project.getName();
                addText("rootsChanged for " + projectName);
            }
        });
        project.getMessageBus().connect().subscribe(ProjectTopics.MODULES, new ModuleListener() {
            @Override
            public void moduleAdded(@NotNull Project project, @NotNull Module module) {
                String projectName = project.getName() + ":" + module.getName();
                addText("moduleAdded for " + projectName);
            }

            @Override
            public void beforeModuleRemoved(@NotNull Project project, @NotNull Module module) {
                String projectName = project.getName() + ":" + module.getName();
                addText("beforeModuleRemoved for " + projectName);
            }

            @Override
            public void moduleRemoved(@NotNull Project project, @NotNull Module module) {
                String projectName = project.getName() + ":" + module.getName();
                addText("moduleRemoved for " + projectName);
            }

            @Override
            public void modulesRenamed(@NotNull Project project, @NotNull List<Module> modules, @NotNull Function<Module, String> oldNameProvider) {
                String projectName = project.getName() + ":\n" + Arrays.stream(modules.toArray()).map(m -> {return oldNameProvider.fun((Module) m) + "->" + ((Module) m).getName();}).collect(Collectors.joining("\n"));
                addText("modulesRenamed for " + projectName);
            }
        });
    }

    @Override
    public void setTextConsumer(@NotNull TextConsumer consumer) {
        textConsumer = consumer;
    }

    @Override
    public void refresh() {
        String projectName = project.getName();
        VirtualFile[] vFiles = ProjectRootManager.getInstance(project).getContentSourceRoots();
        String sourceRootsList = Arrays.stream(vFiles).map(VirtualFile::getUrl).collect(Collectors.joining("\n"));

        addText("Source roots for the " + projectName + " plugin:\n" + sourceRootsList);
    }

    private void addText(@NotNull String text) {
        currentText = currentText + "\n" + text;
        textConsumer.setText(currentText);
    }
}
