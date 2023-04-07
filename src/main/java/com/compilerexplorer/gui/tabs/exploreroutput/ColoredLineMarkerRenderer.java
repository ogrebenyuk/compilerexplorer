package com.compilerexplorer.gui.tabs.exploreroutput;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.LineMarkerRendererEx;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

public class ColoredLineMarkerRenderer implements LineMarkerRendererEx {
    @Nullable
    private Color color;

    public void setColor(@Nullable Color newColor) {
        color = newColor;
    }

    @NotNull
    public Position getPosition() {
        return Position.RIGHT;
    }

    @Override
    public void paint(@NotNull Editor editor, @NotNull Graphics graphics, @NotNull Rectangle rectangle) {
        if (color != null) {
            graphics.setColor(color);
            int h = rectangle.height;
            int w = rectangle.width;
            int[] xPoints = {rectangle.x + w, rectangle.x,     rectangle.x,         rectangle.x + w};
            int[] yPoints = {rectangle.y,     rectangle.y + w, rectangle.y + h - w, rectangle.y + h};
            graphics.fillPolygon(xPoints, yPoints, 4);
        }
    }
}
