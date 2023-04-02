package com.compilerexplorer.gui.tabs;

import com.compilerexplorer.common.Tabs;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class PreprocessorVersionStdoutTabProvider extends BasePreprocessorVersionTabProvider {
    public PreprocessorVersionStdoutTabProvider(@NotNull Project project) {
        super(project, Tabs.PREPROCESSOR_VERSION_STDOUT, "compilerexplorer.ShowPreprocessorVersionStdoutTab", false, false, (unused, output) -> output.getStdout());
    }
}
