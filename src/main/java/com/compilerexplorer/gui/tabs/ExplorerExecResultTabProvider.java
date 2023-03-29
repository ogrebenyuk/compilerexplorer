package com.compilerexplorer.gui.tabs;

import com.compilerexplorer.common.Tabs;
import com.compilerexplorer.datamodel.CompiledText;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.fileTypes.PlainTextFileType;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class ExplorerExecResultTabProvider extends ExplorerTabProvider {
    public ExplorerExecResultTabProvider(@NotNull Project project) {
        super(project, Tabs.EXPLORER_EXEC_RESULT, "compilerexplorer.ShowExplorerExecResultTab", PlainTextFileType.INSTANCE);
    }

    @Override
    public boolean isEnabled(@NotNull CompiledText compiledText) {
        return expectExecResult() && compiledText.isValidExecResult();
    }

    @Override
    public boolean isError(@NotNull CompiledText compiledText) {
        return expectExecResult() && !compiledText.isValidExecResult();
    }

    @Override
    public void provide(@NotNull CompiledText compiledText, @NotNull Function<String, EditorEx> textConsumer) {
        if (compiledText.isValidExecResult()) {
            assert compiledText.compiledResult != null;
            assert compiledText.compiledResult.execResult != null;

            textConsumer.apply(getTextFromChunks(compiledText.compiledResult.execResult.stdout));
        } else if (!expectExecResult()) {
            showExplorerError(compiledText, textConsumer);
        }
    }

    private boolean expectExecResult() {
        return state.getFilters().getExecute();
    }
}
