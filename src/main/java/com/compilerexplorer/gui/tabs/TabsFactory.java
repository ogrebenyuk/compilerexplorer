package com.compilerexplorer.gui.tabs;

import com.google.common.collect.ImmutableList;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TabsFactory {
    @NotNull
    public static List<TabProvider> create(@NotNull Project project) {
        return ImmutableList.of(
                new ProjectInfoTabProvider(project),
                new PreprocessorVersionStdoutTabProvider(project),
                new PreprocessorVersionStderrTabProvider(project),
                new PreprocessorVersionOutputTabProvider(project),
                new PreprocessorStdoutTabProvider(project),
                new PreprocessorStderrTabProvider(project),
                new PreprocessorOutputTabProvider(project),
                new SourceInfoTabProvider(project),
                new ExplorerSiteInfoTabProvider(project),
                new ExplorerSiteRawOutputTabProvider(project),
                new ExplorerRawInputTabProvider(project),
                new ExplorerRawOutputTabProvider(project),
                new ExplorerStdoutTabProvider(project),
                new ExplorerStderrTabProvider(project),
                new ExplorerOutputTabProvider(project),
                new ExplorerExecResultTabProvider(project),
                new EverythingTabProvider(project)
        );
    }
}
