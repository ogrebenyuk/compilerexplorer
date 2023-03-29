package com.compilerexplorer.gui.tabs;

import com.compilerexplorer.common.Tabs;
import com.compilerexplorer.datamodel.CompiledText;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.fileTypes.PlainTextFileType;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class PreprocessorVersionStdoutTabProvider extends TabProvider {
    public PreprocessorVersionStdoutTabProvider(@NotNull Project project) {
        super(project, Tabs.PREPROCESSOR_VERSION_STDOUT, "compilerexplorer.ShowPreprocessorVersionStdoutTab", PlainTextFileType.INSTANCE);
    }

    @Override
    public boolean isEnabled(@NotNull CompiledText compiledText) {
        return !compiledText.sourceRemoteMatched.preprocessedSource.sourceCompilerSettings.isValid();
    }

    @Override
    public boolean isError(@NotNull CompiledText compiledText) {
        return false;
    }

    @Override
    public void provide(@NotNull CompiledText compiledText, @NotNull Function<String, EditorEx> textConsumer) {
        if (compiledText.sourceRemoteMatched.preprocessedSource.sourceCompilerSettings.versionerStdout != null) {
            textConsumer.apply(compiledText.sourceRemoteMatched.preprocessedSource.sourceCompilerSettings.versionerStdout);
        } else if (compiledText.sourceRemoteMatched.preprocessedSource.sourceCompilerSettings.isCached()) {
            textConsumer.apply("Preprocessor was not run because its version was found in cache");
        }
    }
}
