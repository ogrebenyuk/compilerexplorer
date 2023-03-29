package com.compilerexplorer.gui.tabs;

import com.compilerexplorer.common.Tabs;
import com.compilerexplorer.datamodel.CompiledText;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;

import static com.compilerexplorer.datamodel.CompiledText.CompiledResult.*;

public abstract class ExplorerTabProvider extends TabProvider {
    public ExplorerTabProvider(@NotNull Project project_, @NotNull Tabs tab_, @NotNull String actionId_, @NotNull FileType fileType_) {
        super(project_, tab_, actionId_, fileType_);
    }

    protected void showExplorerError(@NotNull CompiledText compiledText, @NotNull Function<String, EditorEx> textConsumer) {
        StringBuilder errorMessageBuilder = new StringBuilder();
        if (compiledText.exception != null) {
            errorMessageBuilder.append("Error: " + compiledText.exception.getMessage() + "\n");
        }
        if (compiledText.compiledResult != null) {
            if (compiledText.compiledResult.code != CODE_NOT_COMPILED
                    && compiledText.compiledResult.code != CODE_GOOD
                    && compiledText.compiledResult.code != CODE_REGULAR_BAD) {
                errorMessageBuilder.append("Compiler Explorer exit code: " + compiledText.compiledResult.code + "\n");
            }
            if (compiledText.compiledResult.stderr != null) {
                buildTextFromChunks(compiledText.compiledResult.stderr, errorMessageBuilder);
            }
        }
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
}
