package com.compilerexplorer.gui.listeners;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.CaretEvent;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class AllEditorsListener {
    @NotNull
    private final Map<Editor, EditorCaretListener> listeners = new HashMap<>();
    @NotNull
    private final Consumer<CaretEvent> caretEventConsumer;

    public AllEditorsListener(@NotNull Project project, @NotNull Consumer<CaretEvent> caretEventConsumer_) {
        caretEventConsumer = caretEventConsumer_;
        Arrays.stream(EditorFactory.getInstance().getAllEditors()).filter(editor -> editor.getProject() == project && FileDocumentManager.getInstance().getFile(editor.getDocument()) != null).forEach(this::addListener);
        EditorFactory.getInstance().addEditorFactoryListener(new EditorLifetimeListener(this::addListener, this::removeListener), ApplicationManager.getApplication());
    }

    private void addListener(@NotNull Editor editor) {
        EditorCaretListener listener = new EditorCaretListener(caretEventConsumer);
        editor.getCaretModel().addCaretListener(listener);
        listeners.put(editor, listener);
    }

    private void removeListener(@NotNull Editor editor) {
        EditorCaretListener listener = listeners.remove(editor);
        if (listener != null) {
            editor.getCaretModel().removeCaretListener(listener);
        }
    }
}
