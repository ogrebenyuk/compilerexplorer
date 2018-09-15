package com.compilerexplorer.project.clion.oc;

import com.compilerexplorer.common.datamodel.ProjectSettings;
import com.compilerexplorer.common.datamodel.SourceSettings;
import com.intellij.openapi.project.Project;
import com.intellij.util.Producer;
import com.jetbrains.cidr.lang.OCLanguageKind;
import com.jetbrains.cidr.lang.toolchains.CidrCompilerSwitches;
import com.jetbrains.cidr.lang.workspace.OCResolveConfiguration;
import com.jetbrains.cidr.lang.workspace.OCWorkspaceRunConfigurationListener;
import com.jetbrains.cidr.lang.workspace.compiler.GCCCompiler;
import com.jetbrains.cidr.lang.workspace.compiler.OCCompilerKind;
import com.jetbrains.cidr.lang.workspace.compiler.OCCompilerSettings;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Objects;
import java.util.Vector;
import java.util.stream.Collectors;

public class OCProjectSettingsProducer implements Producer<ProjectSettings> {
    @NotNull
    private final Project project;

    public OCProjectSettingsProducer(@NotNull Project project_) {
        project = project_;
    }

    @Override
    @NotNull
    public ProjectSettings produce() {
        return collect(project);
    }

    @NotNull
    private static ProjectSettings collect(@NotNull Project project) {
        OCResolveConfiguration configuration = OCWorkspaceRunConfigurationListener.getSelectedResolveConfiguration(project);
        return (configuration != null) ? collect(configuration) : new ProjectSettings(new Vector<>());
    }

    @NotNull
    private static ProjectSettings collect(@NotNull OCResolveConfiguration configuration) {
        OCCompilerSettings compilerSettings = configuration.getCompilerSettings();
        return new ProjectSettings(configuration.getSources().stream().map(virtualFile -> {
            OCLanguageKind language = configuration.getDeclaredLanguageKind(virtualFile);
            if (language != null) {
                File compiler = compilerSettings.getCompilerExecutable(language);
                OCCompilerKind compilerKind = compilerSettings.getCompiler(language);
                CidrCompilerSwitches switches = compilerSettings.getCompilerSwitches(language, virtualFile);
                if (compiler != null && compilerKind != null && switches != null) {
                    return new SourceSettings(virtualFile, language, GCCCompiler.getLanguageOption(language), compiler, compilerKind, switches.getList(CidrCompilerSwitches.Format.RAW));
                }
            }
            return null;
        }).filter(Objects::nonNull).collect(Collectors.toCollection(Vector::new)));
    }
}
