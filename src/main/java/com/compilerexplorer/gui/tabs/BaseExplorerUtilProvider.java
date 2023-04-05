package com.compilerexplorer.gui.tabs;

import com.compilerexplorer.common.Bundle;
import com.compilerexplorer.common.Tabs;
import com.compilerexplorer.common.component.DataHolder;
import com.compilerexplorer.datamodel.CompiledText;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;

import static com.compilerexplorer.datamodel.CompiledText.CompiledResult.*;

public abstract class BaseExplorerUtilProvider extends BaseTabProvider {
    public BaseExplorerUtilProvider(@NotNull Project project_, @NotNull Tabs tab_, @NonNls  @NotNull String actionId_, @NotNull FileType fileType_) {
        super(project_, tab_, actionId_, fileType_);
    }

    protected void showExplorerError(@NotNull CompiledText compiledText, @NotNull Consumer<String> textConsumer) {
        StringBuilder errorMessageBuilder = new StringBuilder();
        compiledText.getException().ifPresent(exception -> {
                    errorMessageBuilder.append(Bundle.format("compilerexplorer.BaseExplorerUtilProvider.Exception", "Exception", exception.getMessage()));
                    errorMessageBuilder.append("\n");
                }
        );
        compiledText.getCompiledResult().ifPresent(compiledResult -> {
            if (compiledResult.code != CODE_NOT_COMPILED
                    && compiledResult.code != CODE_GOOD) {
                errorMessageBuilder.append(Bundle.format("compilerexplorer.BaseExplorerUtilProvider.ExitCode", "Code", Integer.toString(compiledResult.code)));
                errorMessageBuilder.append("\n");
            }
            if (compiledResult.stderr != null) {
                buildTextFromChunks(compiledResult.stderr, errorMessageBuilder);
            }
        });
        textConsumer.accept(errorMessageBuilder.toString());
    }

    protected static boolean hasText(@Nullable List<CompiledText.CompiledChunk> chunks) {
        return chunks != null && chunks.stream().map(c -> c.text).filter(Objects::nonNull).filter(text -> !text.isEmpty()).findFirst().orElse(null) != null;
    }

    @NonNls
    @NotNull
    protected static String getTextFromChunks(@NotNull List<CompiledText.CompiledChunk> chunks) {
        StringBuilder builder = new StringBuilder();
        buildTextFromChunks(chunks, builder);
        return builder.toString();
    }

    protected static void buildTextFromChunks(@NotNull List<CompiledText.CompiledChunk> chunks, @NotNull StringBuilder builder) {
        for (CompiledText.CompiledChunk chunk : chunks) {
            if (chunk.text != null) {
                builder.append(chunk.text);
                builder.append('\n');
            }
        }
    }

    @NotNull
    protected static Optional<CompiledText> compiledText(@NotNull DataHolder data) {
        return data.get(CompiledText.KEY);
    }

    @NotNull
    protected static Optional<CompiledText.CompiledResult> compiledResult(@NotNull DataHolder data) {
        return compiledText(data).flatMap(CompiledText::getCompiledResult);
    }
}
