package com.compilerexplorer.gui.tabs;

import com.compilerexplorer.common.Bundle;
import com.compilerexplorer.common.Tabs;
import com.compilerexplorer.common.component.DataHolder;
import com.compilerexplorer.datamodel.CompiledText;
import com.compilerexplorer.datamodel.state.SettingsState;
import com.intellij.openapi.fileTypes.PlainTextFileType;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ExplorerExecResultTabProvider extends BaseExplorerUtilProvider {
    public ExplorerExecResultTabProvider(@NotNull SettingsState state) {
        super(state, Tabs.EXPLORER_EXEC_RESULT, "compilerexplorer.ShowExplorerExecResultTab", PlainTextFileType.INSTANCE);
    }

    @Override
    public void provide(@NotNull DataHolder data, @NotNull TabContentConsumer contentConsumer) {
        compiledText(data).ifPresentOrElse(
            compiledText -> {
                if (expectExecResult()) {
                    compiledText.getCompiledResultIfGood().ifPresentOrElse(compiledResult -> execResult(compiledText).ifPresentOrElse(
                        execResult -> content(true, () -> getTextFromChunks(execResult.stdout), contentConsumer),
                        () -> content(true, () -> "", contentConsumer)
                    ),
                    () -> {
                        if (compiledText.getCanceled()) {
                            error(true, () -> Bundle.get("compilerexplorer.ExplorerExecResultTabProvider.Canceled"), contentConsumer);
                        } else {
                            error(true, () -> getExplorerError(compiledText), contentConsumer);
                        }
                    });
                } else {
                    message(false, () -> Bundle.get("compilerexplorer.ExplorerExecResultTabProvider.Disabled"), contentConsumer);
                }
            },
            () -> message(false, () -> Bundle.get("compilerexplorer.ExplorerExecResultTabProvider.WasNotRun"), contentConsumer)
        );
    }

    private boolean expectExecResult() {
        return getState().getFilters().getExecute();
    }

    @NotNull
    private Optional<CompiledText.ExecResult> execResult(@NotNull CompiledText compiledText) {
        return compiledText.getExecResult().filter(execResult -> hasText(execResult.stdout));
    }
}
