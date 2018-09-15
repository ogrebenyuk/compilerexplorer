package com.compilerexplorer.gui.listeners;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Caret;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.fileEditor.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
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

        handleExistingEditors();
        subscribeToNewEditors();
        subscribeToSelectedEditorChanges();
    }

    private void handleExistingEditors() {
        Arrays.stream(EditorFactory.getInstance().getAllEditors())
                .filter(editor -> editor.getProject() == project && findFile(editor) != null)
                .forEach(this::addListener);
    }

    private void subscribeToNewEditors() {
        EditorFactory.getInstance().addEditorFactoryListener(new EditorLifetimeListener(this::addListener, this::removeListener), ApplicationManager.getApplication());
    }

    private void subscribeToSelectedEditorChanges() {
        project.getMessageBus().connect().subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, new FileEditorManagerListener() {
            @Override
            public void selectionChanged(@NotNull FileEditorManagerEvent event) {
                if (event.getNewFile() != null) {
                    for (Editor editor : EditorFactory.getInstance().getAllEditors()) {
                        VirtualFile file = findFile(editor);
                        if (event.getNewFile().equals(file)) {
                            caretEventConsumer.accept(file, editor.getCaretModel().getAllCarets());
                        }
                    }
                } else if (event.getOldFile() != null) {
                    caretEventConsumer.accept(event.getOldFile(), new ArrayList<>());
                }
            }
        });
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
