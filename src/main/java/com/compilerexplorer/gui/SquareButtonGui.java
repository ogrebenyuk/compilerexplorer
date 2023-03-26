package com.compilerexplorer.gui;

import com.compilerexplorer.common.ActionListenerIgnoringEvent;
import com.intellij.icons.AllIcons;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class SquareButtonGui {
    @NotNull
    private final JButton button = new JButton();

    public SquareButtonGui(@NotNull Icon icon, @NotNull String tooltip, @NotNull Runnable runnable) {
        button.setIcon(icon);
        button.setToolTipText(tooltip);
        button.addActionListener(new ActionListenerIgnoringEvent(runnable));
        button.setPreferredSize(getMinSquareSize(button));
    }

    @NotNull
    public Component getComponent() {
        return button;
    }

    @NotNull
    static private Dimension getMinSquareSize(@NotNull JComponent component) {
        Dimension minSquareSize = new Dimension();
        minSquareSize.setSize(component.getMinimumSize().getHeight(), component.getMinimumSize().getHeight());
        return minSquareSize;
    }
}
