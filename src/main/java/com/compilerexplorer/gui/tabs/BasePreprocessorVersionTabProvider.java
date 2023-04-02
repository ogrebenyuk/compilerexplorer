package com.compilerexplorer.gui.tabs;

import com.compilerexplorer.common.Tabs;
import com.compilerexplorer.common.component.DataHolder;
import com.compilerexplorer.datamodel.CompilerResult;
import com.compilerexplorer.datamodel.SelectedSourceCompiler;
import com.intellij.json.JsonFileType;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.fileTypes.PlainTextFileType;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;
import java.util.function.Function;

public abstract class BasePreprocessorVersionTabProvider extends BasePreprocessorUtilProvider {
    @NotNull
    private final BiFunction<SelectedSourceCompiler, CompilerResult.Output, String> textProducer;

    public BasePreprocessorVersionTabProvider(@NotNull Project project, @NotNull Tabs tab, @NotNull String actionId, boolean isJson, boolean showError,
                                              @NotNull BiFunction<SelectedSourceCompiler, CompilerResult.Output, String> textProducer_) {
        super(project, tab, actionId, isJson ? JsonFileType.INSTANCE : PlainTextFileType.INSTANCE, showError);
        textProducer = textProducer_;
    }

    @Override
    public void provide(@NotNull DataHolder data, @NotNull Function<String, EditorEx> textConsumer) {
        data.get(SelectedSourceCompiler.KEY).ifPresentOrElse(selectedSourceCompiler ->
                selectedSourceCompiler.getResult().ifPresentOrElse(
                        result -> result.getOutput().ifPresentOrElse(
                                output -> textConsumer.apply(textProducer.apply(selectedSourceCompiler, output)),
                                () -> textConsumer.apply("Preprocessor was not run because of unsupported compiler type")
                        ),
                        () -> textConsumer.apply("Preprocessor was not run because its version was found in cache")
                ),
                () -> textConsumer.apply("Preprocessor was not run")
        );
    }

    @Override
    protected boolean shouldShow(@NotNull DataHolder data) {
        return preprocessorRanButProducedNoResult(data);
    }

    private static boolean preprocessorRanButProducedNoResult(@NotNull DataHolder data) {
        return data.get(SelectedSourceCompiler.KEY).map(BasePreprocessorVersionTabProvider::producedNoResult).orElse(false);
    }

    protected static boolean producedNoResult(@NotNull SelectedSourceCompiler selectedSourceCompiler) {
        return selectedSourceCompiler.getLocalCompilerSettings().isEmpty();
    }
}
