package com.compilerexplorer.gui.tabs;

import com.compilerexplorer.common.Tabs;
import com.compilerexplorer.common.component.DataHolder;
import com.compilerexplorer.datamodel.CompilerResult;
import com.compilerexplorer.datamodel.PreprocessedSource;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.fileTypes.PlainTextFileType;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class PreprocessorStderrTabProvider extends BasePreprocessorTabProvider {
    public PreprocessorStderrTabProvider(@NotNull Project project) {
        super(project, Tabs.PREPROCESSOR_STDERR, "compilerexplorer.ShowPreprocessorStderrTab", false, false, (unused, output) -> output.getStderr());
    }
}
