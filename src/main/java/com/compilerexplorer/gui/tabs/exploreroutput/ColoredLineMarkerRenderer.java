package com.compilerexplorer.gui.tabs.exploreroutput;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.ex.EditorGutterComponentEx;
import com.intellij.openapi.editor.markup.LineMarkerRendererEx;
import com.intellij.openapi.util.registry.Registry;
import com.intellij.util.ui.JBValue;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;

public class ColoredLineMarkerRenderer implements LineMarkerRendererEx {
    private static final boolean IS_NEW_UI = Registry.is("ide.experimental.ui", false);
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
        if (color != null) {
            EditorGutterComponentEx gutter = ((EditorEx) editor).getGutterComponentEx();
            graphics.setColor(color);
            int x = rectangle.x;
            int y = rectangle.y;
            int h = rectangle.height;
            int w = Math.min(gutter.getWhitespaceSeparatorOffset(), gutter.getWidth());
            if (IS_NEW_UI) {
                boolean isFoldingEnabled = ((EditorEx) editor).getFoldingModel().isFoldingEnabled();
                int gap = GAP_BETWEEN_ICONS.get();
                w += isFoldingEnabled ? gap : -gap;
            }
            int d = graphics.getFontMetrics().getHeight() / 3;
            int[] xPoints = {x + w, x + w - d, x + w - d, x + w};
            int[] yPoints = {y,     y + d,     y + h - d, y + h};
            graphics.fillPolygon(xPoints, yPoints, 4);
        }
    }
}
