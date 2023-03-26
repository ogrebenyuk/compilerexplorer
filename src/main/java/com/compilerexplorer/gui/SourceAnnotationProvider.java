package com.compilerexplorer.gui;

import com.compilerexplorer.datamodel.CompiledText;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.colors.EditorFontType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Paths;
import java.util.function.Function;

public class SourceAnnotationProvider extends BaseTextAnnotationGutterProvider {
    @NotNull
    private final Function<Integer, CompiledText.SourceLocation> offsetToSourceLocationMap;

    public SourceAnnotationProvider(@NotNull Function<Integer, CompiledText.SourceLocation> offsetToSourceLocationMap_) {
        offsetToSourceLocationMap = offsetToSourceLocationMap_;
    }

    @Override
    @Nullable
    public String getLineText(int line, @Nullable Editor ed) {
        CompiledText.SourceLocation source = findSource(line, ed);
        return source != null ? getLineText(source) : null;
    }

    @Override
    @Nullable
    public String getToolTip(int line, @Nullable Editor ed) {
        CompiledText.SourceLocation source = findSource(line, ed);
        return source != null ? getTooltipText(source) : null;
    }

    @Override
    @NotNull
    public EditorFontType getStyle(int line, @Nullable Editor ed) {
        return EditorFontType.ITALIC;
    }

    @Nullable
    private CompiledText.SourceLocation findSource(int line, @Nullable Editor ed) {
        return (ed != null) ? offsetToSourceLocationMap.apply(ed.logicalPositionToOffset(new LogicalPosition(line, 0))) : null;
    }

    @NotNull
    private String getLineText(@NotNull CompiledText.SourceLocation source) {
        return source.file != null ? (Paths.get(source.file).getFileName().toString() + ":" + source.line) : "";
    }

    @NotNull
    private String getTooltipText(@NotNull CompiledText.SourceLocation source) {
        return source.file != null ? (source.file + ":" + source.line) : "";
    }
}
