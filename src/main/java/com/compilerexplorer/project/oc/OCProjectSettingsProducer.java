package com.compilerexplorer.project.oc;

import com.compilerexplorer.common.PathNormalizer;
import com.compilerexplorer.common.compilerkind.CompilerKindFactory;
import com.compilerexplorer.datamodel.ProjectSources;
import com.compilerexplorer.datamodel.SourceSettings;
import com.compilerexplorer.project.ProjectSettingsProducer;
import com.intellij.execution.ExecutionException;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.cidr.cpp.cmake.workspace.CMakeProfileInfo;
import com.jetbrains.cidr.cpp.cmake.workspace.CMakeWorkspace;
import com.jetbrains.cidr.cpp.execution.CMakeAppRunConfiguration;
import com.jetbrains.cidr.cpp.execution.CMakeBuildProfileExecutionTarget;
import com.jetbrains.cidr.cpp.toolchains.CPPEnvironment;
import com.jetbrains.cidr.lang.OCLanguageKind;
import com.jetbrains.cidr.lang.toolchains.CidrCompilerSwitches;
import com.jetbrains.cidr.lang.workspace.OCResolveConfiguration;
import com.jetbrains.cidr.lang.workspace.OCWorkspaceRunConfigurationListener;
import com.jetbrains.cidr.lang.workspace.compiler.OCCompilerKind;
import com.jetbrains.cidr.lang.workspace.OCCompilerSettings;
import com.jetbrains.cidr.system.HostMachine;
import com.jetbrains.cidr.system.LocalHost;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Objects;

public class OCProjectSettingsProducer implements ProjectSettingsProducer {
    @Override
    @NotNull
    public ProjectSources get(@NotNull Project project) {
        return collect(project);
    }

    @NotNull
    private static ProjectSources collect(@NotNull Project project) {
        OCResolveConfiguration configuration = OCWorkspaceRunConfigurationListener.getSelectedResolveConfiguration(project);
        return (configuration != null) ? collect(project, configuration) : new ProjectSources(new ArrayList<>());
    }

    @NotNull
    private static ProjectSources collect(@NotNull Project project, @NotNull OCResolveConfiguration configuration) {
        return new ProjectSources(configuration.getSources().stream().sorted(Comparator.comparing(VirtualFile::getName)).map(virtualFile -> {
            OCLanguageKind language = configuration.getDeclaredLanguageKind(virtualFile);
            if (language != null) {
                OCCompilerSettings compilerSettings = configuration.getCompilerSettings(language, virtualFile);
                File compiler = compilerSettings.getCompilerExecutable();
                @Nullable File compilerWorkingDir = compilerSettings.getCompilerWorkingDir();
                OCCompilerKind compilerKind = compilerSettings.getCompilerKind();
                CidrCompilerSwitches switches = compilerSettings.getCompilerSwitches();
                if (compiler != null && compilerKind != null && switches != null) {
                    String compilerKindString = compilerKind.toString();
                    String languageOption = CompilerKindFactory.findCompilerKind(compilerKindString).map(kind -> kind.getLanguageOption(language)).orElse("");
                    return new SourceSettings(
                            PathNormalizer.normalizePath(virtualFile.getPath()),
                            virtualFile.getPresentableName(),
                            language.getDisplayName(),
                            languageOption,
                            compiler.getPath(),
                            compilerWorkingDir != null ? compilerWorkingDir.getPath() : "",
                            compilerKindString,
                            switches.getList(CidrCompilerSwitches.Format.RAW),
                            getHostMachine(project));
                }
            }
            return null;
        }).filter(Objects::nonNull).toList());
    }

    @NotNull
    private static HostMachine getHostMachine(@NotNull Project project) {
        @Nullable CMakeAppRunConfiguration runConfiguration = CMakeAppRunConfiguration.getSelectedRunConfiguration(project);
        @Nullable CMakeBuildProfileExecutionTarget executionTarget = CMakeAppRunConfiguration.getSelectedBuildProfile(project);
        if (runConfiguration != null && executionTarget != null) {
            @Nullable CMakeAppRunConfiguration.BuildAndRunConfigurations buildAndRunConfiguration = runConfiguration.getBuildAndRunConfigurations(executionTarget);
            if (buildAndRunConfiguration != null) {
                @NotNull CMakeWorkspace cMakeWorkspace = CMakeWorkspace.getInstance(project);
                @Nullable CMakeProfileInfo cMakeProfileInfo = null;
                try {
                    cMakeProfileInfo = cMakeWorkspace.getProfileInfoFor(buildAndRunConfiguration.buildConfiguration);
                } catch (ExecutionException e) {
                    // empty
                }
                if (cMakeProfileInfo != null) {
                    @Nullable CPPEnvironment environment = cMakeProfileInfo.getEnvironment();
                    if (environment != null) {
                        return environment.getHostMachine();
                    }
                }
            }
        }
        return LocalHost.INSTANCE;
    }
}
