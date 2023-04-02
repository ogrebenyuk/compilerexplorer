package com.compilerexplorer.gui.tabs;

import com.compilerexplorer.common.Tabs;
import com.compilerexplorer.common.component.DataHolder;
import com.compilerexplorer.datamodel.CompiledText;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.fileTypes.FileType;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Function;

public interface TabProvider {
    class Range {
        public final int begin;
        public final int end;

        Range(int begin_, int end_) {
            begin = begin_;
            end = end_;
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

    void provide(@NotNull DataHolder data, @NotNull Function<String, EditorEx> textConsumer);

    void highlightLocations(@NotNull EditorEx ed, @NotNull List<CompiledText.SourceLocation> highlightedLocations);

    @NotNull
    List<Range> getRangesForLocation(@NotNull CompiledText.SourceLocation location);

    void updateGutter(@NotNull EditorEx ed);

    void updateFolding(@NotNull EditorEx ed);

    void applyThemeColors();
}
