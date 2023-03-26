package com.compilerexplorer.gui.listeners;

import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.event.CaretEvent;
import com.intellij.openapi.editor.event.CaretListener;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class CaretPositionChangeListener implements CaretListener {
    @NotNull
    private final Consumer<LogicalPosition> consumer;

    public CaretPositionChangeListener(@NotNull Consumer<LogicalPosition> consumer_) {
        consumer = consumer_;
    }

    @Override
    public void caretPositionChanged(@NotNull CaretEvent event) {
        consumer.accept(event.getNewPosition());
    }
}
