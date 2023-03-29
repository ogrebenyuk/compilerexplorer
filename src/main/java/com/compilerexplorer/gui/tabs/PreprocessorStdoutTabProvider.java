package com.compilerexplorer.gui.tabs;

import com.compilerexplorer.common.Tabs;
import com.compilerexplorer.datamodel.CompiledText;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.project.Project;
import com.jetbrains.cidr.lang.OCFileType;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

import static com.compilerexplorer.datamodel.PreprocessedSource.CODE_GOOD;

public class PreprocessorStdoutTabProvider extends TabProvider {
    public PreprocessorStdoutTabProvider(@NotNull Project project) {
        super(project, Tabs.PREPROCESSOR_STDOUT, "compilerexplorer.ShowPreprocessorStdoutTab", OCFileType.INSTANCE);
    }

    @Override
    public boolean isEnabled(@NotNull CompiledText compiledText) {
        return compiledText.sourceRemoteMatched.preprocessedSource.preprocessLocally
                && compiledText.sourceRemoteMatched.preprocessedSource.preprocessorExitCode != CODE_GOOD
                ;
    }

    @Override
    public boolean isError(@NotNull CompiledText compiledText) {
        return false;
    }

    @Override
    public void provide(@NotNull CompiledText compiledText, @NotNull Function<String, EditorEx> textConsumer) {
        if (compiledText.sourceRemoteMatched.preprocessedSource.preprocessLocally
                && compiledText.sourceRemoteMatched.preprocessedSource.preprocessedText != null
        ) {
            textConsumer.apply(compiledText.sourceRemoteMatched.preprocessedSource.preprocessedText);
        }
    }
}
