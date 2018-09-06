package com.compilerexplorer.project.clion.oc;

import com.compilerexplorer.common.ProjectSettings;
import com.compilerexplorer.common.ProjectSettingsConsumer;
import com.compilerexplorer.common.SourceSettings;
import com.compilerexplorer.project.common.ProjectSettingsProducer;
import com.intellij.openapi.project.Project;
import com.jetbrains.cidr.lang.OCLanguageKind;
import com.jetbrains.cidr.lang.toolchains.CidrCompilerSwitches;
import com.jetbrains.cidr.lang.workspace.OCResolveConfiguration;
import com.jetbrains.cidr.lang.workspace.OCWorkspaceRunConfigurationListener;
import com.jetbrains.cidr.lang.workspace.compiler.OCCompilerKind;
import com.jetbrains.cidr.lang.workspace.compiler.OCCompilerSettings;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Objects;
import java.util.Vector;
import java.util.stream.Collectors;

public class OCProjectSettingsProducer implements ProjectSettingsProducer {
    @NotNull
    private final Project project;
    @NotNull
    private final ProjectSettingsConsumer projectSettingsConsumer;

    public OCProjectSettingsProducer(@NotNull Project project_, @NotNull ProjectSettingsConsumer projectSettingsConsumer_) {
        project = project_;
        projectSettingsConsumer = projectSettingsConsumer_;
    }

    @Override
    public void projectChanged() {
        projectSettingsConsumer.setProjectSetting(collect(project));
    }

    @NotNull
    private static ProjectSettings collect(@NotNull Project project) {
        Vector<SourceSettings> settings = new Vector<>();
        OCResolveConfiguration configuration = OCWorkspaceRunConfigurationListener.getSelectedResolveConfiguration(project);
        if (configuration != null) {
            OCCompilerSettings compilerSettings = configuration.getCompilerSettings();
            settings = configuration.getSources().stream().map(virtualFile -> {
                OCLanguageKind language = configuration.getDeclaredLanguageKind(virtualFile);
                if (language != null) {
                    File compiler = compilerSettings.getCompilerExecutable(language);
                    OCCompilerKind compilerKind = compilerSettings.getCompiler(language);
                    CidrCompilerSwitches switches = compilerSettings.getCompilerSwitches(language, virtualFile);
                    if (compiler != null && compilerKind != null && switches != null) {
                        return new SourceSettings(virtualFile, language, compiler, compilerKind, switches.getList(CidrCompilerSwitches.Format.RAW));
                    }
                }
                return null;
            }).filter(Objects::nonNull).collect(Collectors.toCollection(Vector::new));
        }
        return new ProjectSettings(settings);
    }
}
