package com.compilerexplorer.gui.tabs;

import com.compilerexplorer.common.Bundle;
import com.compilerexplorer.common.Tabs;
import com.compilerexplorer.common.component.DataHolder;
import com.compilerexplorer.datamodel.CompilerResult;
import com.compilerexplorer.datamodel.SelectedSourceCompiler;
import com.compilerexplorer.datamodel.state.SettingsState;
import com.intellij.json.JsonFileType;
import com.intellij.openapi.fileTypes.PlainTextFileType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public abstract class BasePreprocessorVersionTabProvider extends BasePreprocessorUtilProvider {
    @NotNull
    private final Function<CompilerResult.Output, String> textProducer;

    public BasePreprocessorVersionTabProvider(@NotNull SettingsState state, @NotNull Tabs tab, @NonNls  @NotNull String actionId, boolean isJson,
                                              @NotNull Function<CompilerResult.Output, String> textProducer_) {
        super(state, tab, actionId, isJson ? JsonFileType.INSTANCE : PlainTextFileType.INSTANCE);
        textProducer = textProducer_;
    }

    @Override
    public void provide(@NotNull DataHolder data, @NotNull TabContentConsumer contentConsumer) {
        data.get(SelectedSourceCompiler.KEY).ifPresentOrElse(
            selectedSourceCompiler -> selectedSourceCompiler.getResult().ifPresentOrElse(
                result -> result.getOutput().ifPresentOrElse(
                    output -> content(selectedSourceCompiler.getLocalCompilerSettings().isEmpty(), () -> textProducer.apply(output), contentConsumer),
                    () -> message(() -> textProducer.apply(null), contentConsumer)
                ),
                () -> message(() -> Bundle.get("compilerexplorer.BasePreprocessorVersionTabProvider.Cached"), contentConsumer)
            ),
            () -> message(() -> Bundle.get("compilerexplorer.BasePreprocessorVersionTabProvider.WasNotRun"), contentConsumer)
        );
    }
}
