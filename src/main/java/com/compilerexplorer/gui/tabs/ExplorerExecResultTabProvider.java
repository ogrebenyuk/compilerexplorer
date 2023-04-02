package com.compilerexplorer.gui.tabs;

import com.compilerexplorer.common.Tabs;
import com.compilerexplorer.common.component.DataHolder;
import com.compilerexplorer.datamodel.CompiledText;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.fileTypes.PlainTextFileType;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Function;

public class ExplorerExecResultTabProvider extends BaseExplorerUtilProvider {
    public ExplorerExecResultTabProvider(@NotNull Project project) {
        super(project, Tabs.EXPLORER_EXEC_RESULT, "compilerexplorer.ShowExplorerExecResultTab", PlainTextFileType.INSTANCE);
    }

    @Override
    public boolean isEnabled(@NotNull DataHolder data) {
        return expectExecResult() && execResult(data).isPresent();
    }

    @Override
    public boolean isError(@NotNull DataHolder data) {
        return expectExecResult() && execResult(data).isEmpty();
    }

    @Override
    public void provide(@NotNull DataHolder data, @NotNull Function<String, EditorEx> textConsumer) {
        compiledText(data).ifPresentOrElse(
                compiledText -> compiledText.getExecResult().ifPresentOrElse(
                        execResult -> textConsumer.apply(getTextFromChunks(execResult.stdout)),
                        () -> {
                            if (expectExecResult()) {
                                showExplorerError(compiledText, textConsumer);
                            } else {
                                textConsumer.apply("Compiler Explorer was not asked to execute the code");
                            }
                        }
                ),
                () -> textConsumer.apply("Compiler Explorer was not run")
        );
    }

    private boolean expectExecResult() {
        return state.getFilters().getExecute();
    }

    @NotNull
    private Optional<CompiledText.ExecResult> execResult(@NotNull DataHolder data) {
        return compiledText(data).flatMap(CompiledText::getExecResult);
    }
}
