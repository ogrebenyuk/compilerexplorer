package com.compilerexplorer.gui.tabs;

import com.compilerexplorer.common.Tabs;
import com.compilerexplorer.common.component.DataHolder;
import com.compilerexplorer.datamodel.CompiledText;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface TabProvider {
    @NotNull
    Tabs getTab();

    @NotNull
    String actionId();

    void provide(@NotNull DataHolder data, @NotNull TabContentConsumer contentConsumer);

    void highlightLocations(@NotNull EditorEx ed, @NotNull List<CompiledText.SourceLocation> highlightedLocations);

    @NotNull
    List<TextRange> getRangesForLocation(@NotNull CompiledText.SourceLocation location);

    void editorCreated(@NotNull Project project, @NotNull EditorEx ed);

    void updateGutter(@NotNull Project project, @NotNull EditorEx ed);

    void applyThemeColors();

    boolean isSourceSpecific();
}
