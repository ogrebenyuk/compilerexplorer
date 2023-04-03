package com.compilerexplorer.gui.tabs.exploreroutput;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.ex.EditorGutterComponentEx;
import com.intellij.openapi.editor.markup.LineMarkerRendererEx;
import com.intellij.ui.ExperimentalUI;
import com.intellij.util.ui.JBValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

public class ColoredLineMarkerRenderer implements LineMarkerRendererEx {
    private static final JBValue.JBValueGroup JBVG = new JBValue.JBValueGroup();
    private static final JBValue GAP_BETWEEN_ICONS = JBVG.value(3);

    @Nullable
    private Color color;

    public void setColor(@Nullable Color newColor) {
        color = newColor;
    }

    @NotNull
    public Position getPosition() {
        return Position.CUSTOM;
    }

    @Override
    public void paint(@NotNull Editor editor, @NotNull Graphics graphics, @NotNull Rectangle rectangle) {
        if (color == null) {
            return;
        }
        graphics.setColor(color);
        EditorGutterComponentEx gutter = ((EditorEx) editor).getGutterComponentEx();
        boolean isFoldingEnabled = ((EditorEx) editor).getFoldingModel().isFoldingEnabled();
        int rw = isFoldingEnabled ? gutter.getWhitespaceSeparatorOffset() : rectangle.width;
        if (ExperimentalUI.isNewUI()) {
            rw += isFoldingEnabled ? GAP_BETWEEN_ICONS.get() : -GAP_BETWEEN_ICONS.get();
        }
        int rh = rectangle.height;
        int w = graphics.getFontMetrics().getHeight() / 2;
        int[] xPoints = {rectangle.x + rw, rectangle.x + rw - w, rectangle.x + rw - w, rectangle.x + rw};
        int[] yPoints = {rectangle.y,      rectangle.y + w,      rectangle.y + rh - w, rectangle.y + rh};
        graphics.fillPolygon(xPoints, yPoints, 4);
    }
}
