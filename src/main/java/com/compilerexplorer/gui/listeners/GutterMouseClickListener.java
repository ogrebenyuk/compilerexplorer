package com.compilerexplorer.gui.listeners;

import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class GutterMouseClickListener extends MouseAdapter {
    @NotNull
    private final Consumer<Point> regularClickConsumer;
    @NotNull
    private final BiConsumer<Integer, Integer> popupClickConsumer;

    public GutterMouseClickListener(@NotNull Consumer<Point> regularClickConsumer_, @NotNull BiConsumer<Integer, Integer> popupClickConsumer_) {
        regularClickConsumer = regularClickConsumer_;
        popupClickConsumer = popupClickConsumer_;
    }

    @Override
    public void mouseClicked(@NotNull MouseEvent e) {
        if (!maybePopup(e)) {
            regularClickConsumer.accept(e.getPoint());
            e.consume();
        }
    }
    @Override
    public void mousePressed(@NotNull MouseEvent e) {
        maybePopup(e);
    }
    @Override
    public void mouseReleased(@NotNull MouseEvent e) {
        maybePopup(e);
    }
    private boolean maybePopup(@NotNull MouseEvent e) {
        boolean isPopup = e.isPopupTrigger();
        if (isPopup) {
            popupClickConsumer.accept(e.getX(), e.getY());
            e.consume();
        }
        return isPopup;
    }
}
