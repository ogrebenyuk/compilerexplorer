package com.compilerexplorer.gui;

import com.intellij.openapi.editor.Editor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LineNumberAnnotationProvider extends BaseTextAnnotationGutterProvider {
    @Override
    @NotNull
    public String getLineText(int line, @Nullable Editor ed) {
        return String.valueOf(line + 1);
    }
}
