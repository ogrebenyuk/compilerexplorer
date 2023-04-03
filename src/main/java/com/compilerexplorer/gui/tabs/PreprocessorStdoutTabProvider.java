package com.compilerexplorer.gui.tabs;

import com.compilerexplorer.common.Tabs;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class PreprocessorStdoutTabProvider extends BasePreprocessorTabProvider {
    public PreprocessorStdoutTabProvider(@NotNull Project project) {
        super(project, Tabs.PREPROCESSOR_STDOUT, "compilerexplorer.ShowPreprocessorStdoutTab", true, false, (unused, output) -> output.getStdout());
    }
}
