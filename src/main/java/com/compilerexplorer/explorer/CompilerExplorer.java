package com.compilerexplorer.explorer;

import com.compilerexplorer.common.*;
import com.intellij.ProjectTopics;
import com.intellij.execution.*;
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
import com.jetbrains.cidr.cpp.cmake.model.CMakeConfiguration;
import com.jetbrains.cidr.cpp.cmake.model.CMakeModel;
import com.jetbrains.cidr.cpp.cmake.workspace.CMakeWorkspace;
import com.jetbrains.cidr.cpp.toolchains.CPPToolSet;
import com.jetbrains.cidr.cpp.toolchains.CPPToolchains;
import com.jetbrains.cidr.lang.toolchains.CidrCompilerSwitches;
import com.jetbrains.cidr.lang.workspace.OCResolveConfiguration;
import com.jetbrains.cidr.lang.workspace.OCWorkspaceModificationListener;
import com.jetbrains.cidr.lang.workspace.OCWorkspaceRunConfigurationListener;
import com.jetbrains.cidr.lang.workspace.compiler.OCCompilerSettings;
import com.jetbrains.cidr.lang.workspace.headerRoots.HeadersSearchRoot;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import com.jetbrains.cidr.lang.workspace.OCWorkspace;

public class CompilerExplorer implements PreprocessedSourceConsumer {
    @NotNull
    private final Project project;
    @NotNull
    private final MainTextConsumer textConsumer;
    @NotNull
    private String currentText = "";

    public CompilerExplorer(@NotNull Project project_, @NotNull MainTextConsumer textConsumer_) {
        project = project_;
        textConsumer = textConsumer_;
    }

    @Override
    public void setPreprocessedSource(@NotNull PreprocessedSource preprocessedSource) {
        addText(preprocessedSource.getPreprocessedText());
    }

    @Override
    public void clearPreprocessedSource(@NotNull String reason) {
        addText("CLEAR " + reason);
    }

    private void addText(@NotNull String text) {
        currentText = currentText + "\n" + text;
        textConsumer.setMainText(currentText);
    }

/*
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
            addText("module " + module.getName());
        }

        for(RunConfiguration conf : RunManager.getInstance(project).getAllConfigurationsList()) {
            addText("RunConfiguration: name " + conf.getName());
        }

        for (RunnerAndConfigurationSettings se : RunManager.getInstance(project).getAllSettings()) {
            addText("RunnerAndConfigurationSettings: name " + se.getName() + ", unique id " + se.getUniqueID() + ", folder name " + se.getFolderName() + ", configuration " + se.getConfiguration().getName() + ", type display name " + se.getType().getDisplayName());
        }

        RunnerAndConfigurationSettings se = RunManager.getInstance(project).getSelectedConfiguration();
        addText("selected RunnerAndConfigurationSettings: name " + se.getName() + ", unique id " + se.getUniqueID() + ", folder name " + se.getFolderName() + ", configuration " + se.getConfiguration().getName() + ", type display name " + se.getType().getDisplayName());

        OCWorkspace workspace = OCWorkspace.getInstance(project);
        CMakeWorkspace cworkspace = CMakeWorkspace.getInstance(project);
        addText("CMakeWorkspace getSourceFiles:\n" + cworkspace.getSourceFiles().stream().map(File::toString).collect(Collectors.joining("\n")));
        for (OCResolveConfiguration configuration : workspace.getConfigurations()) {
            addText("OCResolveConfiguration: getDisplayName(true) " + configuration.getDisplayName(true) +
                    ", getDisplayName(false) " + configuration.getDisplayName(false) +
                    ", getUniqueId() " + configuration.getUniqueId());
            OCCompilerSettings compilerSettings = configuration.getCompilerSettings();
            addText("getSources():\n" + configuration.getSources().stream().map(v -> {
                    return v.getUrl()
                            + ", lang " + configuration.getDeclaredLanguageKind(v).getDisplayName()
                            + "\ncompiler " + compilerSettings.getCompiler(configuration.getDeclaredLanguageKind(v)).toString() + ", " + compilerSettings.getCompilerExecutable(configuration.getDeclaredLanguageKind(v)).getAbsolutePath()
                            + ", CompilerWorkingDir " + compilerSettings.getCompilerWorkingDir().getAbsolutePath()
                            + "\nswitches " + compilerSettings.getCompilerSwitches(configuration.getDeclaredLanguageKind(v), v).getList(CidrCompilerSwitches.Format.RAW).stream().collect(Collectors.joining(" "))
                            + "\nkey " + compilerSettings.getCompilerKey(configuration.getDeclaredLanguageKind(v), v).getValue()
                            + "\ngetLibraryHeadersRoots:\n" + configuration.getLibraryHeadersRoots(configuration.getDeclaredLanguageKind(v), v).stream().map(HeadersSearchRoot::toString).collect(Collectors.joining("\n"))
                            ;
                    }
            ).collect(Collectors.joining("\n")));

            CPPToolchains toolchains = CPPToolchains.getInstance();
            addText(toolchains.getToolchains().stream().map(t -> t.getName() + " " + t.getToolSetPath() + " " + t.getToolSetKind().getDisplayName() + " " + t.getToolSetOptions().stream().map(CPPToolSet.Option::getValue).collect(Collectors.joining(" "))).collect(Collectors.joining("\n")));

            //addText("project component adapters:\n" + project.getPicoContainer().getComponentAdapters().stream().map(o -> o == null ? "" : o.toString()).collect(Collectors.joining("\n")));

            CMakeConfiguration cconf = cworkspace.getCMakeConfigurationFor(configuration);
            addText("CMakeConfiguration: name " + cconf.getName()
                    + ", profile name " + cconf.getProfileName()
                    + ", build type " + cconf.getBuildType()
                    + ", " + cconf.toString()
                    + "\ntarget " + cconf.getTarget().toString()+ ", " + cconf.getTargetType()
                    + "\nsources " + cconf.getSources().stream().map(f -> f.toString() + ", settings " + cconf.getFileSettings(f).toString() + ", " + cconf.getCombinedCompilerFlags(cconf.getFileSettings(f).getLanguageKind(), f)).collect(Collectors.joining(" "))
                    + "\ngetBuildWorkingDir " + cconf.getBuildWorkingDir()
                    + "\ngetConfigurationGenerationDir " + cconf.getConfigurationGenerationDir()
                    + "\ngetProductFile " + cconf.getProductFile()
            );

            CMakeModel model = cworkspace.getModel();
            addText("CMakeModel: " + model.getProjectName() + ", getHeaderAndResourceFiles " + model.getHeaderAndResourceFiles().stream().map(File::toString).collect(Collectors.joining(" ")));
        }

        ExecutionTargetManager targetManager = ExecutionTargetManager.getInstance(project);
        addText("getActiveTarget: " + targetManager.getActiveTarget().getDisplayName() + ", " + targetManager.getActiveTarget().getId() + ", " + targetManager.getActiveTarget().toString());
        RunManagerEx runManager = RunManagerEx.getInstanceEx(project);
        RunnerAndConfigurationSettings selected = runManager.getSelectedConfiguration();
        addText("getSelectedConfiguration: " + selected.getName() + ", " + selected.getType().getDisplayName() + ", " + selected.getType().getConfigurationTypeDescription()
                + ", " + selected.getFolderName() + ", " + selected.getConfiguration().getPresentableType()
        );
        addText("getAllSettings:\n" + runManager.getAllSettings().stream().map(s ->
            s.getName() + ", " + s.getType().getDisplayName() + ", " + s.getType().getConfigurationTypeDescription()
                    + ", " + s.getFolderName() + ", " + s.getConfiguration().getPresentableType()
                    + "; getTargetsFor: " + targetManager.getTargetsFor(s).stream().map(t -> targetManager.getActiveTarget().getDisplayName() + ", " + targetManager.getActiveTarget().getId() + ", " + targetManager.getActiveTarget().toString()).collect(Collectors.joining("|"))
        ).collect(Collectors.joining("\n")));
        addText("getAllConfigurationsList:\n" + runManager.getAllConfigurationsList().stream().map(s ->
                s.getName() + ", " + s.getType().getDisplayName() + ", " + s.getType().getConfigurationTypeDescription()
                + ", " + s.getPresentableType()
        ).collect(Collectors.joining("\n")));

        addText("getSelectedResolveConfiguration: " + OCWorkspaceRunConfigurationListener.getSelectedResolveConfiguration(project).getDisplayName(false) + "\n");

    }
*/
}
