package com.compilerexplorer.gui.tabs.exploreroutput;

import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.TextAnnotationGutterProvider;
import com.intellij.openapi.editor.colors.ColorKey;
import com.intellij.openapi.editor.colors.EditorColors;
import com.intellij.openapi.editor.colors.EditorFontType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.List;

public abstract class BaseTextAnnotationGutterProvider implements TextAnnotationGutterProvider {
    @Override
    @Nullable
    public String getToolTip(int line, @Nullable Editor ed) {
        return null;
    }
    @Override
    @NotNull
    public EditorFontType getStyle(int line, @Nullable Editor ed) {
        return EditorFontType.PLAIN;
    }
    @Override
    @Nullable
    public ColorKey getColor(int line, @Nullable Editor ed) {
        return EditorColors.LINE_NUMBERS_COLOR;
    }
    @Override
    @Nullable
    public Color getBgColor(int line, @Nullable Editor ed) {
        return null;
    }
    @Override
    @Nullable
    public List<AnAction> getPopupActions(int line, @Nullable Editor ed) {
        return null;
    }
    @Override
    public void gutterClosed() {
    }
}
