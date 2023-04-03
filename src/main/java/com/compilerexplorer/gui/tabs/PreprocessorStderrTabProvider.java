package com.compilerexplorer.gui.tabs;

import com.compilerexplorer.common.Tabs;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class PreprocessorStderrTabProvider extends BasePreprocessorTabProvider {
    public PreprocessorStderrTabProvider(@NotNull Project project) {
        super(project, Tabs.PREPROCESSOR_STDERR, "compilerexplorer.ShowPreprocessorStderrTab", false, false, (unused, output) -> output.getStderr());
    }
}
