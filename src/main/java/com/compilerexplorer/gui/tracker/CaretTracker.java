package com.compilerexplorer.gui.tracker;

import com.compilerexplorer.common.PathNormalizer;
import com.compilerexplorer.datamodel.CompiledText;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public class CaretTracker {
    @NotNull
    private final Consumer<List<CompiledText.SourceLocation>> locationsConsumer;
    @NotNull
    private List<CompiledText.SourceLocation> locations = new ArrayList<>();

    public CaretTracker(@NotNull Consumer<List<CompiledText.SourceLocation>> locationsConsumer_) {
        locationsConsumer = locationsConsumer_;
    }

    public void update(@NotNull VirtualFile file, @Nullable Editor editor) {
        List<CompiledText.SourceLocation> newLocations = editor != null ? collectLocations(file, editor) : new ArrayList<>();
        if (!newLocations.equals(locations)) {
            locations = newLocations;
            locationsConsumer.accept(locations);
        }
    }

    @NotNull
    private List<CompiledText.SourceLocation> collectLocations(@NotNull VirtualFile file, @NotNull Editor editor) {
        return collectLineNumbers(editor).stream().
                map(line -> new CompiledText.SourceLocation(PathNormalizer.normalizePath(file.getPath()), line + 1)).
                collect(Collectors.toList());
    }

    @NotNull
    public List<CompiledText.SourceLocation> getLocations() {
        return locations;
    }

    @NotNull
    private List<Integer> collectLineNumbers(@NotNull Editor editor) {
        return Stream.concat(
                editor.getCaretModel().getAllCarets().stream().map(caret -> caret.getLogicalPosition().line),
                IntStream.rangeClosed(editor.offsetToLogicalPosition(editor.getSelectionModel().getSelectionStart()).line, editor.offsetToLogicalPosition(editor.getSelectionModel().getSelectionEnd()).line).boxed()
        ).distinct().collect(Collectors.toList());
    }
}
