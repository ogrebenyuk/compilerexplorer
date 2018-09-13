package com.compilerexplorer.gui.tracker;

import com.compilerexplorer.common.datamodel.CompiledText;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class CaretTracker {
    @NotNull
    private final Consumer<List<CompiledText.SourceLocation>> locationsConsumer;
    @Nullable
    private List<CompiledText.SourceLocation> locations;

    public CaretTracker(@NotNull Consumer<List<CompiledText.SourceLocation>> locationsConsumer_) {
        locationsConsumer = locationsConsumer_;
    }

    public void update(@NotNull VirtualFile file, @NotNull List<Caret> carets) {
        List<CompiledText.SourceLocation> newLocations = collectLocations(file, carets);
        if (!newLocations.equals(locations)) {
            locations = newLocations;
            refresh();
        }
    }

    @NotNull
    private List<CompiledText.SourceLocation> collectLocations(@NotNull VirtualFile file, @NotNull List<Caret> carets) {
        return carets.stream().
                map(c -> new CompiledText.SourceLocation(file.getPath(), c.getLogicalPosition().line + 1)).
                collect(Collectors.toList());
    }

    public void refresh() {
        if (locations != null) {
            locationsConsumer.accept(locations);
        }
    }
}
