package com.compilerexplorer.gui.listeners;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.EditorFactoryEvent;
import com.intellij.openapi.editor.event.EditorFactoryListener;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

class EditorLifetimeListener implements EditorFactoryListener {
    @NotNull
    private final Consumer<Editor> editorCreatedConsumer;
    @NotNull
    private final Consumer<Editor> editorReleasedConsumer;

    EditorLifetimeListener(@NotNull Consumer<Editor> editorCreatedConsumer_, @NotNull Consumer<Editor> editorReleasedConsumer_) {
        editorCreatedConsumer = editorCreatedConsumer_;
        editorReleasedConsumer = editorReleasedConsumer_;
    }

    @Override
    public void editorCreated(@NotNull EditorFactoryEvent event) {
        editorCreatedConsumer.accept(event.getEditor());
    }
    @Override
    public void editorReleased(@NotNull EditorFactoryEvent event) {
        editorReleasedConsumer.accept(event.getEditor());
    }
}
