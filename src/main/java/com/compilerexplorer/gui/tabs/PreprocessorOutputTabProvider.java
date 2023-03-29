package com.compilerexplorer.gui.tabs;

import com.compilerexplorer.common.Tabs;
import com.compilerexplorer.datamodel.CompiledText;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.project.Project;
import com.jetbrains.cidr.lang.OCFileType;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

import static com.compilerexplorer.datamodel.PreprocessedSource.CODE_GOOD;

public class PreprocessorOutputTabProvider extends PreprocessorTabProvider {
    public PreprocessorOutputTabProvider(@NotNull Project project) {
        super(project, Tabs.PREPROCESSOR_OUTPUT, "compilerexplorer.ShowPreprocessorOutputTab", OCFileType.INSTANCE);
    }

    @Override
    public boolean isEnabled(@NotNull CompiledText compiledText) {
        return compiledText.sourceRemoteMatched.preprocessedSource.sourceCompilerSettings.isValid();
    }

    @Override
    public boolean isError(@NotNull CompiledText compiledText) {
        return compiledText.sourceRemoteMatched.preprocessedSource.sourceCompilerSettings.isValid()
                && compiledText.sourceRemoteMatched.preprocessedSource.preprocessLocally
                && compiledText.sourceRemoteMatched.preprocessedSource.preprocessorExitCode != CODE_GOOD;
    }

    @Override
    public void provide(@NotNull CompiledText compiledText, @NotNull Function<String, EditorEx> textConsumer) {
        if (compiledText.sourceRemoteMatched.preprocessedSource.sourceCompilerSettings.isValid()) {
            if (!compiledText.sourceRemoteMatched.preprocessedSource.preprocessLocally
                    || compiledText.sourceRemoteMatched.preprocessedSource.preprocessorExitCode == CODE_GOOD
            ) {
                textConsumer.apply(compiledText.sourceRemoteMatched.preprocessedSource.preprocessedText);
            } else {
                showPreprocessorError(compiledText.sourceRemoteMatched.preprocessedSource.preprocessorException,
                        compiledText.sourceRemoteMatched.preprocessedSource.preprocessorExitCode,
                        compiledText.sourceRemoteMatched.preprocessedSource.preprocessorStderr,
                        textConsumer);
            }
        }
    }
}
