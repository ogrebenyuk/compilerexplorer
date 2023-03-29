package com.compilerexplorer.gui.tabs;

import com.compilerexplorer.common.Tabs;
import com.compilerexplorer.datamodel.CompiledText;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.fileTypes.PlainTextFileType;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class PreprocessorStderrTabProvider extends TabProvider {
    public PreprocessorStderrTabProvider(@NotNull Project project) {
        super(project, Tabs.PREPROCESSOR_STDERR, "compilerexplorer.ShowPreprocessorStderrTab", PlainTextFileType.INSTANCE);
    }

    @Override
    public boolean isEnabled(@NotNull CompiledText compiledText) {
        return compiledText.sourceRemoteMatched.preprocessedSource.preprocessLocally
                && compiledText.sourceRemoteMatched.preprocessedSource.preprocessorStderr != null
                && !compiledText.sourceRemoteMatched.preprocessedSource.preprocessorStderr.isEmpty()
                ;
    }

    @Override
    public boolean isError(@NotNull CompiledText compiledText) {
        return false;
    }

    @Override
    public void provide(@NotNull CompiledText compiledText, @NotNull Function<String, EditorEx> textConsumer) {
        if (compiledText.sourceRemoteMatched.preprocessedSource.preprocessLocally
                && compiledText.sourceRemoteMatched.preprocessedSource.preprocessorStderr != null
        ) {
            textConsumer.apply(compiledText.sourceRemoteMatched.preprocessedSource.preprocessorStderr);
        }
    }
}
