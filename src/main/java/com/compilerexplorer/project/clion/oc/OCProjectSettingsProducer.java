package com.compilerexplorer.project.clion.oc;

import com.compilerexplorer.common.PathNormalizer;
import com.compilerexplorer.datamodel.ProjectSettings;
import com.compilerexplorer.datamodel.SourceSettings;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.cidr.lang.OCLanguageKind;
import com.jetbrains.cidr.lang.toolchains.CidrCompilerSwitches;
import com.jetbrains.cidr.lang.workspace.OCResolveConfiguration;
import com.jetbrains.cidr.lang.workspace.OCWorkspaceRunConfigurationListener;
import com.jetbrains.cidr.lang.workspace.compiler.GCCCompiler;
import com.jetbrains.cidr.lang.workspace.compiler.OCCompilerKind;
import com.jetbrains.cidr.lang.workspace.OCCompilerSettings;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Comparator;
import java.util.Objects;
import java.util.Vector;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class OCProjectSettingsProducer implements Supplier<ProjectSettings> {
    @NotNull
    private final Project project;

    public OCProjectSettingsProducer(@NotNull Project project_) {
        project = project_;
    }

    @Override
    @NotNull
    public ProjectSettings get() {
        return collect(project);
    }

    @NotNull
    private static ProjectSettings collect(@NotNull Project project) {
        OCResolveConfiguration configuration = OCWorkspaceRunConfigurationListener.getSelectedResolveConfiguration(project);
        return (configuration != null) ? collect(configuration) : new ProjectSettings(new Vector<>());
    }

    @NotNull
    private static ProjectSettings collect(@NotNull OCResolveConfiguration configuration) {
        return new ProjectSettings(configuration.getSources().stream().sorted(Comparator.comparing(VirtualFile::getName)).map(virtualFile -> {
            OCLanguageKind language = configuration.getDeclaredLanguageKind(virtualFile);
            if (language != null) {
                OCCompilerSettings compilerSettings = configuration.getCompilerSettings(language, virtualFile);
                File compiler = compilerSettings.getCompilerExecutable();
                OCCompilerKind compilerKind = compilerSettings.getCompilerKind();
                CidrCompilerSwitches switches = compilerSettings.getCompilerSwitches();
                if (compiler != null && compilerKind != null && switches != null) {
                    return new SourceSettings(configuration,  virtualFile, PathNormalizer.normalizePath(virtualFile.getPath()), language.getDisplayName(), GCCCompiler.getLanguageOption(language), compiler, compilerKind.toString(), switches.getList(CidrCompilerSwitches.Format.RAW));
                }
            }
            return null;
        }).filter(Objects::nonNull).collect(Collectors.toCollection(Vector::new)));
    }
}
