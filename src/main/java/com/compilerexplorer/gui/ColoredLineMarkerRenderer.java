package com.compilerexplorer.gui;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.markup.LineMarkerRenderer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

public class ColoredLineMarkerRenderer implements LineMarkerRenderer {
    @Nullable
    private Color color;

    void setColor(@Nullable Color newColor) {
        color = newColor;
    }

    @Override
    public void paint(@NotNull Editor editor, @NotNull Graphics graphics, @NotNull Rectangle rectangle) {
        if (color == null) {
            return;
        }
        graphics.setColor(color);
        int margin = rectangle.width;
        int[] xPoints = {rectangle.x + margin, rectangle.x, rectangle.x, rectangle.x + margin};
        int[] yPoints = {rectangle.y, rectangle.y + margin, rectangle.y + rectangle.height - margin, rectangle.y + rectangle.height};
        graphics.fillPolygon(xPoints, yPoints, 4);
    }
}
