package com.compilerexplorer.gui.listeners;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class AllEditorsListener {
    @NotNull
    private final Project project;
    @NotNull
    private final Map<Editor, EditorCaretListener> listeners;
    @NotNull
    private final BiConsumer<VirtualFile, List<Caret>> caretEventConsumer;

    public AllEditorsListener(@NotNull Project project_, @NotNull BiConsumer<VirtualFile, List<Caret>> caretEventConsumer_) {
        project = project_;
        listeners = new HashMap<>();
        caretEventConsumer = caretEventConsumer_;
        Arrays.stream(EditorFactory.getInstance().getAllEditors()).
                filter(editor -> editor.getProject() == project && findFile(editor) != null).
                forEach(this::addListener);
        EditorFactory.getInstance().addEditorFactoryListener(new EditorLifetimeListener(this::addListener, this::removeListener), ApplicationManager.getApplication());
    }

    private void addListener(@NotNull Editor editor) {
        EditorCaretListener listener = new EditorCaretListener(event -> {
            if (event.getEditor().getProject() == project) {
                VirtualFile file = findFile(event.getEditor());
                if (file != null) {
                    caretEventConsumer.accept(file, event.getEditor().getCaretModel().getAllCarets());
                }
            }
        });
        editor.getCaretModel().addCaretListener(listener);
        listeners.put(editor, listener);
    }

    private void removeListener(@NotNull Editor editor) {
        EditorCaretListener listener = listeners.remove(editor);
        if (listener != null) {
            editor.getCaretModel().removeCaretListener(listener);
        }
    }

    @Nullable
    private VirtualFile findFile(@NotNull Editor editor) {
        return FileDocumentManager.getInstance().getFile(editor.getDocument());
    }
}
