package com.compilerexplorer.gui.listeners;

import org.jetbrains.annotations.NotNull;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.BiConsumer;

public class GutterMousePopupClickListener extends MouseAdapter {
    @NotNull
    private final BiConsumer<Integer, Integer> popupClickConsumer;

    public GutterMousePopupClickListener(@NotNull BiConsumer<Integer, Integer> popupClickConsumer_) {
        popupClickConsumer = popupClickConsumer_;
    }

    @Override
    public void mouseClicked(@NotNull MouseEvent e) {
        maybePopup(e);
    }

    @Override
    public void mousePressed(@NotNull MouseEvent e) {
        maybePopup(e);
    }

    @Override
    public void mouseReleased(@NotNull MouseEvent e) {
        maybePopup(e);
    }

    private void maybePopup(@NotNull MouseEvent e) {
        if (e.isPopupTrigger()) {
            popupClickConsumer.accept(e.getX(), e.getY());
            e.consume();
        }
    }
}
