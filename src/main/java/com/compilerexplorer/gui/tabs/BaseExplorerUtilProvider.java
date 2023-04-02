package com.compilerexplorer.gui.tabs;

import com.compilerexplorer.common.Tabs;
import com.compilerexplorer.common.component.DataHolder;
import com.compilerexplorer.datamodel.CompiledText;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Function;

import static com.compilerexplorer.datamodel.CompiledText.CompiledResult.*;

public abstract class BaseExplorerUtilProvider extends BaseTabProvider {
    public BaseExplorerUtilProvider(@NotNull Project project_, @NotNull Tabs tab_, @NotNull String actionId_, @NotNull FileType fileType_) {
        super(project_, tab_, actionId_, fileType_);
    }

    protected void showExplorerError(@NotNull CompiledText compiledText, @NotNull Function<String, EditorEx> textConsumer) {
        StringBuilder errorMessageBuilder = new StringBuilder();
        compiledText.getException().ifPresent(exception ->
            errorMessageBuilder.append("Error: " + exception.getMessage() + "\n")
        );
        compiledText.getCompiledResult().ifPresent(compiledResult -> {
            if (compiledResult.code != CODE_NOT_COMPILED
                    && compiledResult.code != CODE_GOOD
                    && compiledResult.code != CODE_REGULAR_BAD) {
                errorMessageBuilder.append("Compiler Explorer exit code: " + compiledResult.code + "\n");
            }
            if (compiledResult.stderr != null) {
                buildTextFromChunks(compiledResult.stderr, errorMessageBuilder);
            }
        });
        textConsumer.apply(errorMessageBuilder.toString());
    }

    protected static boolean hasText(@Nullable List<CompiledText.CompiledChunk> chunks) {
        return chunks != null && chunks.stream().map(c -> c.text).filter(Objects::nonNull).filter(text -> !text.isEmpty()).findFirst().orElse(null) != null;
    }

    @NotNull
    protected String getTextFromChunks(@NotNull List<CompiledText.CompiledChunk> chunks) {
        StringBuilder builder = new StringBuilder();
        buildTextFromChunks(chunks, builder);
        return builder.toString();
    }

    protected void buildTextFromChunks(@NotNull List<CompiledText.CompiledChunk> chunks, @NotNull StringBuilder builder) {
        for (CompiledText.CompiledChunk chunk : chunks) {
            if (chunk.text != null) {
                builder.append(chunk.text);
                builder.append('\n');
            }
        }
    }

    @NotNull
    protected Optional<CompiledText> compiledText(@NotNull DataHolder data) {
        return data.get(CompiledText.KEY);
    }

    @NotNull
    protected Optional<CompiledText.CompiledResult> compiledResult(@NotNull DataHolder data) {
        return compiledText(data).flatMap(CompiledText::getCompiledResult);
    }
}
