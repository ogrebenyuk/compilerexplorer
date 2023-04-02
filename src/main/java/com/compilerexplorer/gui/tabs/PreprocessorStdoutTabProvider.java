package com.compilerexplorer.gui.tabs;

import com.compilerexplorer.common.Tabs;
import com.compilerexplorer.common.component.DataHolder;
import com.compilerexplorer.datamodel.CompilerResult;
import com.compilerexplorer.datamodel.PreprocessedSource;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.project.Project;
import com.jetbrains.cidr.lang.OCFileType;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class PreprocessorStdoutTabProvider extends BasePreprocessorTabProvider {
    public PreprocessorStdoutTabProvider(@NotNull Project project) {
        super(project, Tabs.PREPROCESSOR_STDOUT, "compilerexplorer.ShowPreprocessorStdoutTab", true, false, (unused, output) -> output.getStdout());
    }
}
