package com.compilerexplorer.project.bazel;

import com.compilerexplorer.common.PathNormalizer;
import com.compilerexplorer.common.compilerkind.CompilerKind;
import com.compilerexplorer.common.compilerkind.CompilerKindFactory;
import com.compilerexplorer.datamodel.ProjectSources;
import com.compilerexplorer.datamodel.SourceSettings;
import com.compilerexplorer.project.ProjectSettingsProducer;
import com.google.common.collect.Streams;
import com.google.idea.blaze.base.ideinfo.*;
import com.google.idea.blaze.base.model.BlazeProjectData;
import com.google.idea.blaze.base.model.primitives.ExecutionRootPath;
import com.google.idea.blaze.base.run.BlazeCommandRunConfiguration;
import com.google.idea.blaze.base.settings.Blaze;
import com.google.idea.blaze.base.sync.workspace.WorkspacePathResolver;
import com.intellij.execution.RunManager;
import com.intellij.execution.RunnerAndConfigurationSettings;
import com.intellij.openapi.project.Project;
import com.jetbrains.cidr.lang.CLanguageKind;
import com.jetbrains.cidr.lang.CUDALanguageKind;
import com.jetbrains.cidr.lang.OCLanguageKind;
import com.jetbrains.cidr.system.LocalHost;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

public class BazelProjectSettingsProducer implements ProjectSettingsProducer {
    @NotNull
    private static final OCLanguageKind CPP_LANGUAGE = CLanguageKind.CPP;
    @NotNull
    private static final OCLanguageKind CUDA_LANGUAGE = CUDALanguageKind.CUDA;

    @Override
    @NotNull
    public ProjectSources get(@NotNull Project project) {
        List<SourceSettings> sources = new ArrayList<>();
        @Nullable BlazeProjectData projectData = Blaze.getProjectData(project);
        @Nullable RunnerAndConfigurationSettings configuration = RunManager.getInstance(project).getSelectedConfiguration();
        if (projectData != null && configuration != null && configuration.getConfiguration() instanceof BlazeCommandRunConfiguration bazelConfiguration && !bazelConfiguration.getTargets().isEmpty()) {
            @NotNull TargetMap targetMap = projectData.getTargetMap();
            @NotNull WorkspacePathResolver workspacePathResolver = projectData.getWorkspacePathResolver();
            targetMap.map().forEach((k, v) -> {
                if (k.getLabel().equals(bazelConfiguration.getTargets().get(0))) {
                    @Nullable CIdeInfo info = v.getcIdeInfo();
                    @Nullable CToolchainIdeInfo toolchainInfo = info == null ? null : findToolchain(v, targetMap);
                    if (info != null && toolchainInfo != null) {
                        @NotNull File compilerPath = getAbsolutePath(toolchainInfo.getCppExecutable(), workspacePathResolver);
                        Optional<CompilerKind> compilerKind = CompilerKindFactory.findCompilerKindFromExecutableFilename(compilerPath.getName());
                        info.getSources().forEach(source -> {
                            if (source.isSource() && compilerKind.isPresent()) {
                                OCLanguageKind language = compilerKind.get().isCuda() ? CUDA_LANGUAGE : CPP_LANGUAGE;
                                File path = workspacePathResolver.resolveToFile(source.getRelativePath());
                                sources.add(new SourceSettings(
                                        PathNormalizer.normalizePath(path.getAbsolutePath()),
                                        path.getName(),
                                        language.getDisplayName(),
                                        compilerKind.get().getLanguageOption(language),
                                        PathNormalizer.normalizePath(compilerPath.getAbsolutePath()),
                                        PathNormalizer.normalizePath(workspacePathResolver.findPackageRoot(".").getAbsolutePath()),
                                        compilerKind.get().getKind(),
                                        getSwitches(toolchainInfo, info, compilerKind.get()),
                                        LocalHost.INSTANCE
                                ));
                            }
                        });
                    }
                }
            });
        }
        return new ProjectSources(sources);
    }

    @Nullable
    private static CToolchainIdeInfo findToolchain(@NotNull TargetIdeInfo targetIdeInfo, @NotNull TargetMap targetMap) {
        if (targetIdeInfo.getcToolchainIdeInfo() != null) {
            return targetIdeInfo.getcToolchainIdeInfo();
        }
        for (@NotNull Dependency dependency: targetIdeInfo.getDependencies()) {
            @Nullable TargetIdeInfo dependencyInfo = targetMap.get(dependency.getTargetKey());
            if (dependencyInfo != null && dependencyInfo.getcToolchainIdeInfo() != null) {
                return dependencyInfo.getcToolchainIdeInfo();
            }
        }
        return null;
    }

    @NotNull
    private static File getAbsolutePath(@NotNull ExecutionRootPath cppExecutable, @NotNull WorkspacePathResolver workspacePathResolver) {
        File path = cppExecutable.getAbsoluteOrRelativeFile();
        if (cppExecutable.isAbsolute()) {
            return path;
        } else {
            return workspacePathResolver.resolveToFile(path.getPath());
        }
    }

    @NotNull
    private static List<String> getSwitches(@NotNull CToolchainIdeInfo toolchainInfo, @NotNull CIdeInfo info, @NotNull CompilerKind compilerKind) {
        return Streams.concat(
            toolchainInfo.getCppCompilerOptions().stream(),
            info.getLocalCopts().stream(),
            info.getTransitiveDefines().stream().map(define -> compilerKind.getDefineOption() + define),
            getIncludes(info.getTransitiveSystemIncludeDirectories(), compilerKind.getSystemIncludeOption()),
            getIncludes(info.getTransitiveIncludeDirectories(), compilerKind.getIncludeOption()),
            getIncludes(info.getTransitiveQuoteIncludeDirectories(), compilerKind.getQuoteIncludeOption())
        ).toList();
    }

    @NotNull
    private static Stream<String> getIncludes(@NotNull List<ExecutionRootPath> paths, @NonNls @NotNull String includeSwitch) {
        return paths.stream()
                .map(path -> includeSwitch + path.getAbsoluteOrRelativeFile().getPath());
    }
}
