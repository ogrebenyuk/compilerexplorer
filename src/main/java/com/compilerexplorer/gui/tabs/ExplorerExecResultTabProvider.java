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
                        () -> error(true, () -> getExecError(compiledText), contentConsumer)
                    ),
                    () -> {
                        if (compiledText.getCanceled()) {
                            error(true, () -> Bundle.get("compilerexplorer.ExplorerExecResultTabProvider.Canceled"), contentConsumer);
                        } else {
                            error(true, () -> getExplorerError(compiledText), contentConsumer);
                        }
                    });
                } else {
                    message(() -> Bundle.get("compilerexplorer.ExplorerExecResultTabProvider.Disabled"), contentConsumer);
                }
            },
            () -> message(() -> Bundle.get("compilerexplorer.ExplorerExecResultTabProvider.WasNotRun"), contentConsumer)
        );
    }

    private boolean expectExecResult() {
        return getState().getFilters().getExecute();
    }

    @NotNull
    private Optional<CompiledText.ExecResult> execResult(@NotNull CompiledText compiledText) {
        return compiledText.getExecResult().filter(execResult -> execResult.didExecute);
    }

    @NotNull
    private String getExecError(@NotNull CompiledText compiledText) {
        StringBuilder errorMessageBuilder = new StringBuilder();
        compiledText.getExecResult().ifPresent(execResult -> {
            if (execResult.buildResult != null) {
                if (execResult.buildResult.code != 0) {
                    errorMessageBuilder.append(Bundle.format("compilerexplorer.ExplorerExecResultTabProvider.BuildExitCode", "Code", Integer.toString(execResult.buildResult.code)));
                    errorMessageBuilder.append("\n");
                }
                if (execResult.buildResult.stdout != null) {
                    buildTextFromChunks(execResult.buildResult.stdout, errorMessageBuilder);
                }
                if (execResult.buildResult.stderr != null) {
                    buildTextFromChunks(execResult.buildResult.stderr, errorMessageBuilder);
                }
            }
            if (execResult.code != 0) {
                errorMessageBuilder.append(Bundle.format("compilerexplorer.ExplorerExecResultTabProvider.ExitCode", "Code", Integer.toString(execResult.code)));
                errorMessageBuilder.append("\n");
            }
            if (execResult.stdout != null) {
                buildTextFromChunks(execResult.stdout, errorMessageBuilder);
            }
            if (execResult.stderr != null) {
                buildTextFromChunks(execResult.stderr, errorMessageBuilder);
            }
        });
        return errorMessageBuilder.toString();
    }
}
