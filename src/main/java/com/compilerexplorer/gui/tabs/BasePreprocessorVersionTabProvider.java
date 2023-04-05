package com.compilerexplorer.gui.tabs;

import com.compilerexplorer.common.Bundle;
import com.compilerexplorer.common.Tabs;
import com.compilerexplorer.common.component.DataHolder;
import com.compilerexplorer.datamodel.CompilerResult;
import com.compilerexplorer.datamodel.SelectedSourceCompiler;
import com.intellij.json.JsonFileType;
import com.intellij.openapi.fileTypes.PlainTextFileType;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;
import java.util.function.Consumer;

public abstract class BasePreprocessorVersionTabProvider extends BasePreprocessorUtilProvider {
    @NotNull
    private final BiFunction<SelectedSourceCompiler, CompilerResult.Output, String> textProducer;

    public BasePreprocessorVersionTabProvider(@NotNull Project project, @NotNull Tabs tab, @NonNls  @NotNull String actionId, boolean isJson, boolean showError,
                                              @NotNull BiFunction<SelectedSourceCompiler, CompilerResult.Output, String> textProducer_) {
        super(project, tab, actionId, isJson ? JsonFileType.INSTANCE : PlainTextFileType.INSTANCE, showError);
        textProducer = textProducer_;
    }

    @Override
    public void provide(@NotNull DataHolder data, @NotNull Consumer<String> textConsumer) {
        data.get(SelectedSourceCompiler.KEY).ifPresentOrElse(selectedSourceCompiler -> {
                if (!selectedSourceCompiler.getCanceled()) {
                    selectedSourceCompiler.getResult().ifPresentOrElse(
                        result -> result.getOutput().ifPresentOrElse(
                            output -> textConsumer.accept(textProducer.apply(selectedSourceCompiler, output)),
                            () -> textConsumer.accept(Bundle.get("compilerexplorer.BasePreprocessorVersionTabProvider.UnsupportedCompilerType"))
                        ),
                        () -> textConsumer.accept(Bundle.get("compilerexplorer.BasePreprocessorVersionTabProvider.Cached"))
                    );
                } else {
                    textConsumer.accept(Bundle.get("compilerexplorer.BasePreprocessorVersionTabProvider.Canceled"));
                }
            },
            () -> textConsumer.accept(Bundle.get("compilerexplorer.BasePreprocessorVersionTabProvider.WasNotRun"))
        );
    }

    @Override
    protected boolean shouldShow(@NotNull DataHolder data) {
        return preprocessorRanButProducedNoResult(data);
    }

    private boolean preprocessorRanButProducedNoResult(@NotNull DataHolder data) {
        return data.get(SelectedSourceCompiler.KEY).map(this::producedNoResult).orElse(false);
    }

    protected boolean producedNoResult(@NotNull SelectedSourceCompiler selectedSourceCompiler) {
        return selectedSourceCompiler.getLocalCompilerSettings().isEmpty();
    }
}
