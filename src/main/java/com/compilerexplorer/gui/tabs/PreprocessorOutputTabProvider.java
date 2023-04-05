package com.compilerexplorer.gui.tabs;

import com.compilerexplorer.common.Tabs;
import com.compilerexplorer.datamodel.CompilerResult;
import com.compilerexplorer.datamodel.PreprocessedSource;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public class PreprocessorOutputTabProvider extends BasePreprocessorTabProvider {
    public PreprocessorOutputTabProvider(@NotNull Project project) {
        super(project, Tabs.PREPROCESSOR_OUTPUT, "compilerexplorer.ShowPreprocessorOutputTab", true, true, PreprocessorOutputTabProvider::getText);
    }

    @Nls
    @NotNull
    private static String getText(@NotNull PreprocessedSource preprocessedSource, @NotNull CompilerResult.Output output) {
        if (producedNoResult(preprocessedSource)) {
            return getPreprocessorErrorMessage(output);
        } else {
            return preprocessedSource.getPreprocessedText().orElse("");
        }
    }
}
