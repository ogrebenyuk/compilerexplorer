package com.compilerexplorer.gui.tabs;

import com.compilerexplorer.common.Bundle;
import com.compilerexplorer.common.Tabs;
import com.compilerexplorer.common.component.DataHolder;
import com.compilerexplorer.datamodel.PreprocessedSource;
import com.compilerexplorer.datamodel.state.SettingsState;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public class PreprocessorOutputTabProvider extends BasePreprocessorTabProvider {
    public PreprocessorOutputTabProvider(@NotNull SettingsState state) {
        super(state, Tabs.PREPROCESSOR_OUTPUT, "compilerexplorer.ShowPreprocessorOutputTab", true, unused -> "");
    }

    @Override
    public void provide(@NotNull DataHolder data, @NotNull TabContentConsumer contentConsumer) {
        data.get(PreprocessedSource.KEY).ifPresentOrElse(preprocessedSource -> {
                if (!preprocessedSource.getCanceled()) {
                    preprocessedSource.getResult().ifPresentOrElse(result -> result.getOutput().ifPresent(output -> {
                            if (preprocessedSource.getPreprocessedText().isPresent()) {
                                content(true, () -> getText(preprocessedSource), contentConsumer);
                            } else {
                                error(true, () -> getPreprocessorErrorMessage(result, output), contentConsumer);
                            }
                        }),
                        () -> message(() -> Bundle.get("compilerexplorer.BasePreprocessorTabProvider.Disabled"), contentConsumer)
                    );
                } else {
                    error(true, () -> Bundle.get("compilerexplorer.BasePreprocessorTabProvider.Canceled"), contentConsumer);
                }
            },
            () -> message(() -> Bundle.get("compilerexplorer.BasePreprocessorTabProvider.WasNotRun"), contentConsumer)
        );
    }

    @Nls
    @NotNull
    private static String getText(@NotNull PreprocessedSource preprocessedSource) {
        return preprocessedSource.getPreprocessedText().orElse("");
    }
}
