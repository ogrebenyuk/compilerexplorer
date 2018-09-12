package com.compilerexplorer.gui.listeners;

import com.intellij.openapi.editor.event.CaretEvent;
import com.intellij.openapi.editor.event.CaretListener;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

class EditorCaretListener implements CaretListener {
    @NotNull
    private final Consumer<CaretEvent> caretEventConsumer;

    EditorCaretListener(@NotNull Consumer<CaretEvent> caretEventConsumer_) {
        caretEventConsumer = caretEventConsumer_;
    }

    @Override
    public void caretPositionChanged(CaretEvent event) {
        caretEventConsumer.accept(event);
    }
}
