package com.compilerexplorer.gui.tabs;

import com.compilerexplorer.common.Tabs;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class PreprocessorVersionStderrTabProvider extends BasePreprocessorVersionTabProvider {
    public PreprocessorVersionStderrTabProvider(@NotNull Project project) {
        super(project, Tabs.PREPROCESSOR_VERSION_STDERR, "compilerexplorer.ShowPreprocessorVersionStderrTab", false, false, (unused, output) -> output.getStderr());
    }
}
