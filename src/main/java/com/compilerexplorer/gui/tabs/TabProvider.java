package com.compilerexplorer.gui.tabs;

import com.compilerexplorer.common.Tabs;
import com.compilerexplorer.common.component.DataHolder;
import com.compilerexplorer.datamodel.CompiledText;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.util.TextRange;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;

public interface TabProvider {
    class FoldingRegion {
        @NotNull
        public final TextRange range;
        @NotNull
        public final String label;
        @NotNull
        public final String placeholderText;

        public FoldingRegion(@NotNull TextRange range_, @NotNull String label_, @NotNull String placeholderText_) {
            range = range_;
            label = label_;
            placeholderText = placeholderText_;
        }
    }

    @NotNull
    Tabs getTab();

    @NotNull
    String actionId();

    @NotNull
    FileType getFileType(@NotNull DataHolder data);

    boolean isEnabled(@NotNull DataHolder data);

    boolean isError(@NotNull DataHolder data);

    void provide(@NotNull DataHolder data, @NotNull BiConsumer<String, Optional<List<FoldingRegion>>> textAndFoldingConsumer);

    void highlightLocations(@NotNull EditorEx ed, @NotNull List<CompiledText.SourceLocation> highlightedLocations);

    @NotNull
    List<TextRange> getRangesForLocation(@NotNull CompiledText.SourceLocation location);

    void editorCreated(@NotNull EditorEx ed);

    void updateGutter(@NotNull EditorEx ed);

    void applyThemeColors();

    boolean isSourceSpecific();

    @NotNull
    String defaultExtension(@NotNull DataHolder data);
}
