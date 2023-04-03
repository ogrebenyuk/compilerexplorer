package com.compilerexplorer.gui.tabs.exploreroutput;

import com.intellij.openapi.editor.Editor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

public class ByteOffsetAnnotationProvider extends BaseTextAnnotationGutterProvider {
    @NotNull
    private final Function<Integer, Integer> lineNumberToByteOffsetMap;

    public ByteOffsetAnnotationProvider(@NotNull Function<Integer, Integer> lineNumberToByteOffsetMap_) {
        lineNumberToByteOffsetMap = lineNumberToByteOffsetMap_;
    }

    @Override
    @Nullable
    public String getLineText(int line, @Nullable Editor ed) {
        Integer byteOffset = lineNumberToByteOffsetMap.apply(line);
        return byteOffset != null ? getLineText(byteOffset) : null;
    }
    @Override
    @Nullable
    public String getToolTip(int line, @Nullable Editor ed) {
        Integer byteOffset = lineNumberToByteOffsetMap.apply(line);
        return byteOffset != null ? getTooltipText(byteOffset) : null;
    }

    @NotNull
    private String getLineText(@NotNull Integer byteOffset) {
        return Integer.toHexString(byteOffset);
    }

    @NotNull
    private String getTooltipText(@NotNull Integer byteOffset) {
        return "Byte offset " + byteOffset + ", hex 0x" + Integer.toHexString(byteOffset);
    }
}
