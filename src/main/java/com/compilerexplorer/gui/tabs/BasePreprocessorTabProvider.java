package com.compilerexplorer.gui.tabs;

import com.compilerexplorer.common.Tabs;
import com.compilerexplorer.common.component.DataHolder;
import com.compilerexplorer.datamodel.CompilerResult;
import com.compilerexplorer.datamodel.PreprocessedSource;
import com.compilerexplorer.datamodel.SelectedSource;
import com.intellij.openapi.fileTypes.PlainTextFileType;
import com.intellij.openapi.project.Project;
import com.jetbrains.cidr.lang.OCFileType;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;
import java.util.function.Consumer;

public abstract class BasePreprocessorTabProvider extends BasePreprocessorUtilProvider {
    @NotNull
    private static final String DEFAULT_PREPROCESSED_TEXT_EXTENSION = "ii";

    private final boolean showWhenSourcePresent;
    @NotNull
    private final BiFunction<PreprocessedSource, CompilerResult.Output, String> textProducer;

    public BasePreprocessorTabProvider(@NotNull Project project, @NotNull Tabs tab, @NotNull String actionId, boolean isCpp, boolean showWhenSourcePresent_,
                                       @NotNull BiFunction<PreprocessedSource, CompilerResult.Output, String> textProducer_) {
        super(project, tab, actionId, isCpp ? OCFileType.INSTANCE : PlainTextFileType.INSTANCE, false);
        showWhenSourcePresent = showWhenSourcePresent_;
        textProducer = textProducer_;
    }

    @Override
    public void provide(@NotNull DataHolder data, @NotNull Consumer<String> textConsumer) {
        data.get(PreprocessedSource.KEY).ifPresentOrElse(preprocessedSource -> {
                if (!preprocessedSource.getCanceled()) {
                    preprocessedSource.getResult().ifPresentOrElse(
                        result -> result.getOutput().ifPresent(
                            output -> textConsumer.accept(textProducer.apply(preprocessedSource, output))
                        ),
                        () -> textConsumer.accept("Preprocessor was not run because local preprocessing is disabled")
                    );
                } else {
                    textConsumer.accept("Preprocessor was canceled");
                }
            },
            () -> textConsumer.accept("Preprocessor was not run")
        );
    }

    @Override
    protected boolean shouldShow(@NotNull DataHolder data) {
        return (showWhenSourcePresent && preprocessorShouldHaveRun(data)) || preprocessorRanButProducedNoResult(data);
    }

    @Override
    protected boolean shouldShowError(@NotNull DataHolder data) {
        return showWhenSourcePresent ? data.get(PreprocessedSource.KEY).map(ps -> producedNoResult(ps) || ps.getResult().flatMap(CompilerResult::getOutput).isEmpty()).orElse(true)
                                     : data.get(PreprocessedSource.KEY).flatMap(PreprocessedSource::getResult).flatMap(CompilerResult::getOutput).isEmpty();
    }

    @Override
    @NotNull
    public String defaultExtension(@NotNull DataHolder data) {
        return getFileType(data) == PlainTextFileType.INSTANCE ? super.defaultExtension(data) : DEFAULT_PREPROCESSED_TEXT_EXTENSION;
    }

    private static boolean preprocessorShouldHaveRun(@NotNull DataHolder data) {
        return data.get(SelectedSource.KEY).isPresent();
    }

    private static boolean preprocessorRanButProducedNoResult(@NotNull DataHolder data) {
        return data.get(PreprocessedSource.KEY).map(BasePreprocessorTabProvider::producedNoResult).orElse(false);
    }

    protected static boolean producedNoResult(@NotNull PreprocessedSource preprocessedSource) {
        return preprocessedSource.getPreprocessedText().isEmpty();
    }
}
