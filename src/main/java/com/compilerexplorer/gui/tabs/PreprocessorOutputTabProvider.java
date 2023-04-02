package com.compilerexplorer.gui.tabs;

import com.compilerexplorer.common.Tabs;
import com.compilerexplorer.common.component.DataHolder;
import com.compilerexplorer.datamodel.CompilerResult;
import com.compilerexplorer.datamodel.PreprocessedSource;
import com.compilerexplorer.datamodel.SelectedSourceCompiler;
import com.compilerexplorer.datamodel.json.JsonSerializationVisitor;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.project.Project;
import com.jetbrains.cidr.lang.OCFileType;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class PreprocessorOutputTabProvider extends BasePreprocessorTabProvider {
    public PreprocessorOutputTabProvider(@NotNull Project project) {
        super(project, Tabs.PREPROCESSOR_OUTPUT, "compilerexplorer.ShowPreprocessorOutputTab", true, true, PreprocessorOutputTabProvider::getText);
    }

    @NotNull
    private static String getText(@NotNull PreprocessedSource preprocessedSource, @NotNull CompilerResult.Output output) {
        if (producedNoResult(preprocessedSource)) {
            return getPreprocessorErrorMessage(output);
        } else {
            return preprocessedSource.getPreprocessedText().orElse("");
        }
    }
}
