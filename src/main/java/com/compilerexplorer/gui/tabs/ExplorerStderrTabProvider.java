package com.compilerexplorer.gui.tabs;

import com.compilerexplorer.common.Tabs;
import com.compilerexplorer.datamodel.CompiledText;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.fileTypes.PlainTextFileType;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class ExplorerStderrTabProvider extends ExplorerTabProvider {
    public ExplorerStderrTabProvider(@NotNull Project project) {
        super(project, Tabs.EXPLORER_STDERR, "compilerexplorer.ShowExplorerStderrTab", PlainTextFileType.INSTANCE);
    }

    @Override
    public boolean isEnabled(@NotNull CompiledText compiledText) {
        return compiledText.compiledResult != null && hasText(compiledText.compiledResult.stderr);
    }

    @Override
    public boolean isError(@NotNull CompiledText compiledText) {
        return false;
    }

    @Override
    public void provide(@NotNull CompiledText compiledText, @NotNull Function<String, EditorEx> textConsumer) {
        if (compiledText.compiledResult != null && compiledText.compiledResult.stderr != null) {
            textConsumer.apply(getTextFromChunks(compiledText.compiledResult.stderr));
        }
    }
}
