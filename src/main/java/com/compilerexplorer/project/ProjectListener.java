package com.compilerexplorer.project;

import com.compilerexplorer.common.ProjectSettings;
import com.compilerexplorer.common.ProjectSettingsConsumer;
import com.compilerexplorer.common.SourceSettings;
import com.intellij.ProjectTopics;
import com.intellij.execution.*;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.ModuleListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootEvent;
import com.intellij.openapi.roots.ModuleRootListener;
import com.intellij.util.Function;
import com.jetbrains.cidr.lang.OCLanguageKind;
import com.jetbrains.cidr.lang.toolchains.CidrCompilerSwitches;
import com.jetbrains.cidr.lang.workspace.OCResolveConfiguration;
import com.jetbrains.cidr.lang.workspace.OCWorkspaceModificationListener;
import com.jetbrains.cidr.lang.workspace.OCWorkspaceRunConfigurationListener;
import com.jetbrains.cidr.lang.workspace.compiler.OCCompilerSettings;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Vector;
import java.util.stream.Collectors;

public class ProjectListener {
    @NotNull
    private final Project project;
    @NotNull
    private final ProjectSettingsConsumer consumer;

    public ProjectListener(@NotNull Project project_, @NotNull ProjectSettingsConsumer consumer_) {
        project = project_;
        consumer = consumer_;
        subscribe();
    }

    public void refresh() {
        consumer.setProjectSetting(collect(project));
    }

    private void subscribe() {
        project.getMessageBus().connect().subscribe(RunManagerListener.TOPIC, new RunManagerListener() {
            public void runConfigurationSelected() {
                refresh();
            }
            public void runConfigurationAdded(@NotNull RunnerAndConfigurationSettings conf) {
                refresh();
            }
            public void runConfigurationRemoved(@NotNull RunnerAndConfigurationSettings conf) {
                refresh();
            }
            public void runConfigurationChanged(@NotNull RunnerAndConfigurationSettings conf) {
                refresh();
            }
        });
        project.getMessageBus().connect().subscribe(ExecutionTargetManager.TOPIC, e -> this.refresh());
        project.getMessageBus().connect().subscribe(OCWorkspaceModificationListener.TOPIC, new OCWorkspaceModificationListener() {
            public void projectsChanged() {
                refresh();
            }
            public void projectFilesChanged() {
                refresh();
            }
            public void sourceFilesChanged() {
                refresh();
            }
            public void buildSettingsChanged() {
                refresh();
            }
            public void selectedResolveConfigurationChanged() {
                refresh();
            }
            public void buildFinished() {
                refresh();
            }
        });
        project.getMessageBus().connect().subscribe(ProjectTopics.MODULES, new ModuleListener() {
            @Override
            public void moduleAdded(@NotNull Project project, @NotNull Module module) {
                refresh();
            }
            @Override
            public void beforeModuleRemoved(@NotNull Project project, @NotNull Module module) {
                refresh();
            }
            @Override
            public void moduleRemoved(@NotNull Project project, @NotNull Module module) {
                refresh();
            }
            @Override
            public void modulesRenamed(@NotNull Project project, @NotNull List<Module> modules, @NotNull Function<Module, String> oldNameProvider) {
                refresh();
            }
        });
        project.getMessageBus().connect().subscribe(ProjectTopics.PROJECT_ROOTS, new ModuleRootListener() {
            @Override
            public void rootsChanged(ModuleRootEvent event) {
                refresh();
            }
        });
    }

    @NotNull
    private static ProjectSettings collect(@NotNull Project project) {
        Vector<SourceSettings> settings = new Vector<>();
        OCResolveConfiguration configuration = OCWorkspaceRunConfigurationListener.getSelectedResolveConfiguration(project);
        if (configuration != null) {
            OCCompilerSettings compilerSettings = configuration.getCompilerSettings();
            settings = configuration.getSources().stream().map(virtualFile -> {
                OCLanguageKind lang = configuration.getDeclaredLanguageKind(virtualFile);
                if (lang != null) {
                    File compiler = compilerSettings.getCompilerExecutable(lang);
                    if (compiler != null) {
                        CidrCompilerSwitches switches = compilerSettings.getCompilerSwitches(lang, virtualFile);
                        if (switches != null) {
                            return new SourceSettings(virtualFile, compiler, switches.getList(CidrCompilerSwitches.Format.RAW));
                        }
                    }
                }
                return null;
            }).filter(Objects::nonNull).collect(Collectors.toCollection(Vector::new));
        }
        return new ProjectSettings(settings);
    }
}
