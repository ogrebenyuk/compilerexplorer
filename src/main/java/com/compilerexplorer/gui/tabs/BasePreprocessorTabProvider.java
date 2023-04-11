package com.compilerexplorer.gui.tabs;

import com.compilerexplorer.common.Bundle;
import com.compilerexplorer.common.Constants;
import com.compilerexplorer.common.Tabs;
import com.compilerexplorer.common.component.DataHolder;
import com.compilerexplorer.datamodel.CompilerResult;
import com.compilerexplorer.datamodel.PreprocessedSource;
import com.compilerexplorer.datamodel.state.SettingsState;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.PlainTextFileType;
import com.jetbrains.cidr.lang.OCFileType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public abstract class BasePreprocessorTabProvider extends BasePreprocessorUtilProvider {
    @NotNull
    private final Function<CompilerResult.Output, String> textProducer;

    public BasePreprocessorTabProvider(@NotNull SettingsState state, @NotNull Tabs tab, @NonNls @NotNull String actionId, boolean isCpp,
                                       @NotNull Function<CompilerResult.Output, String> textProducer_) {
        super(state, tab, actionId, isCpp ? OCFileType.INSTANCE : PlainTextFileType.INSTANCE);
        textProducer = textProducer_;
    }

    @Override
    public void provide(@NotNull DataHolder data, @NotNull TabContentConsumer contentConsumer) {
        data.get(PreprocessedSource.KEY).ifPresentOrElse(
            preprocessedSource -> preprocessedSource.getResult().ifPresentOrElse(
                result -> result.getOutput().ifPresentOrElse(
                    output -> content(preprocessedSource.getPreprocessedText().isEmpty(), () -> textProducer.apply(output), contentConsumer),
                    () -> message(() -> textProducer.apply(null), contentConsumer)
                ),
                () -> message(() -> Bundle.get("compilerexplorer.BasePreprocessorTabProvider.Disabled"), contentConsumer)
            ),
            () -> message(() -> Bundle.get("compilerexplorer.BasePreprocessorTabProvider.WasNotRun"), contentConsumer)
        );
    }

    @Override
    @NonNls
    @NotNull
    protected String defaultExtension(@NotNull FileType filetype) {
        return filetype == PlainTextFileType.INSTANCE ? super.defaultExtension(filetype) : Constants.DEFAULT_PREPROCESSED_TEXT_EXTENSION;
    }
}
