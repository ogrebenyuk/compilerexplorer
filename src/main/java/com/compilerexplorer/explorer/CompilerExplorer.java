package com.compilerexplorer.explorer;

import com.compilerexplorer.base.Explorer;
import com.compilerexplorer.base.handlers.TextConsumer;
import com.intellij.ProjectTopics;
import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.execution.configurations.RunConfiguration;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleManager;
import com.intellij.openapi.project.ModuleListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.projectRoots.Sdk;
import com.intellij.openapi.roots.ModuleRootEvent;
import com.intellij.openapi.roots.ModuleRootListener;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Function;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.jetbrains.cidr.lang.psi.*;

public class CompilerExplorer implements Explorer {
    @NotNull
    private final Project project;

    private TextConsumer textConsumer;

    @NotNull
    private String currentText = "";

    public CompilerExplorer(@NotNull Project project_) {
        project = project_;

        project.getMessageBus().connect().subscribe(ProjectTopics.PROJECT_ROOTS, new ModuleRootListener() {
            @Override
            public void beforeRootsChange(ModuleRootEvent event) {
                addText("beforeRootsChange for " + project.getName());
            }

            @Override
            public void rootsChanged(ModuleRootEvent event) {
                addText("rootsChanged for " + project.getName());
            }
        });
        project.getMessageBus().connect().subscribe(ProjectTopics.MODULES, new ModuleListener() {
            @Override
            public void moduleAdded(@NotNull Project project, @NotNull Module module) {
                addText("moduleAdded for " + project.getName() + ":" + module.getName());
            }

            @Override
            public void beforeModuleRemoved(@NotNull Project project, @NotNull Module module) {
                addText("beforeModuleRemoved for " + project.getName() + ":" + module.getName());
            }

            @Override
            public void moduleRemoved(@NotNull Project project, @NotNull Module module) {
                addText("moduleRemoved for " + project.getName() + ":" + module.getName());
            }

            @Override
            public void modulesRenamed(@NotNull Project project, @NotNull List<Module> modules, @NotNull Function<Module, String> oldNameProvider) {
                addText("modulesRenamed for " + project.getName() + ":\n" + Arrays.stream(modules.toArray()).map(m -> oldNameProvider.fun((Module) m) + "->" + ((Module) m).getName()).collect(Collectors.joining("\n")));
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
        addText("Source roots for the " + projectName + " plugin:\n" + Arrays.stream(ProjectRootManager.getInstance(project).getContentSourceRoots()).map(VirtualFile::getUrl).collect(Collectors.joining("\n")));
        addText("Roots for the " + projectName + " plugin:\n" + Arrays.stream(ProjectRootManager.getInstance(project).getContentRoots()).map(VirtualFile::getUrl).collect(Collectors.joining("\n")));
        addText("getContentRootsFromAllModules for the " + projectName + " plugin:\n" + Arrays.stream(ProjectRootManager.getInstance(project).getContentRootsFromAllModules()).map(VirtualFile::getUrl).collect(Collectors.joining("\n")));

        Sdk projectSdk = ProjectRootManager.getInstance(project).getProjectSdk();
        if (projectSdk != null) {
            addText("SDK: name " + projectSdk.getName() + ", version " + projectSdk.getVersionString() + ", home path " + projectSdk.getHomePath() + ", type " + projectSdk.getSdkType().getName());
        } else {
            addText("No SDK");
        }
        addText("SDK name: " + ProjectRootManager.getInstance(project).getProjectSdkName());

        Module[] modules = ModuleManager.getInstance(project).getModules();
        for (Module module : modules)
        {
            addText("module " + module.getName() /*+ ", type " + module.getModuleTypeName()*/);
        }

        for(RunConfiguration conf : RunManager.getInstance(project).getAllConfigurationsList()) {
            addText("RunConfiguration: name " + conf.getName() /*+ ", presentable type " + conf.getPresentableType()*/);
        }

        for (RunnerAndConfigurationSettings se : RunManager.getInstance(project).getAllSettings()) {
            addText("RunnerAndConfigurationSettings: name " + se.getName() + ", unique id " + se.getUniqueID() + ", folder name " + se.getFolderName() + ", configuration " + se.getConfiguration().getName() + ", type display name " + se.getType().getDisplayName());
        }
    }

    private void addText(@NotNull String text) {
        currentText = currentText + "\n" + text;
        textConsumer.setText(currentText);
    }
}
