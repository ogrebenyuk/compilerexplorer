package com.compilerexplorer.gui.listeners;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.CaretEvent;
import com.intellij.openapi.editor.event.CaretListener;
import com.intellij.openapi.editor.event.SelectionListener;
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
    private final Map<Editor, CaretListener> caretListeners;
    @NotNull
    private final Map<Editor, SelectionListener> selectionListeners;
    @NotNull
    private final BiConsumer<VirtualFile, Editor> editorEventConsumer;

    public AllEditorsListener(@NotNull Project project_, @NotNull BiConsumer<VirtualFile, Editor> editorEventConsumer_) {
        project = project_;
        caretListeners = new HashMap<>();
        selectionListeners = new HashMap<>();
        editorEventConsumer = editorEventConsumer_;

        handleExistingEditors();
        subscribeToNewEditors();
        subscribeToSelectedEditorChanges();
    }

    private void handleExistingEditors() {
        Arrays.stream(EditorFactory.getInstance().getAllEditors())
                .filter(editor -> editor.getProject() == project && findFile(editor) != null)
                .forEach(this::addListeners);
    }

    private void subscribeToNewEditors() {
        EditorFactory.getInstance().addEditorFactoryListener(new EditorLifetimeListener(this::addListeners, this::removeListeners), ApplicationManager.getApplication());
    }

    private void subscribeToSelectedEditorChanges() {
        project.getMessageBus().connect().subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, new FileEditorManagerListener() {
            @Override
            public void selectionChanged(@NotNull FileEditorManagerEvent event) {
                if (event.getNewFile() != null) {
                    for (Editor editor : EditorFactory.getInstance().getAllEditors()) {
                        VirtualFile file = findFile(editor);
                        if (event.getNewFile().equals(file)) {
                            editorEventConsumer.accept(file, editor);
                        }
                    }
                } else if (event.getOldFile() != null) {
                    editorEventConsumer.accept(event.getOldFile(), null);
                }
            }
        });
    }

    private void addListeners(@NotNull Editor editor) {
        CaretListener caretListener = new CaretListener() {
            @Override
            public void caretPositionChanged(CaretEvent event) {
                handleEditorEvent(event.getEditor());
            }
        };
        editor.getCaretModel().addCaretListener(caretListener);
        caretListeners.put(editor, caretListener);

        SelectionListener selectionListener = event -> handleEditorEvent(event.getEditor());
        editor.getSelectionModel().addSelectionListener(selectionListener);
        selectionListeners.put(editor, selectionListener);
    }

    private void handleEditorEvent(@NotNull Editor editor) {
        if (editor.getProject() == project) {
            VirtualFile file = findFile(editor);
            if (file != null) {
                editorEventConsumer.accept(file, editor);
            }
        }
    }

    private void removeListeners(@NotNull Editor editor) {
        CaretListener caretListener = caretListeners.remove(editor);
        if (caretListener != null) {
            editor.getCaretModel().removeCaretListener(caretListener);
        }

        SelectionListener selectionListener = selectionListeners.remove(editor);
        if (selectionListener != null) {
            editor.getSelectionModel().removeSelectionListener(selectionListener);
        }
    }

    @Nullable
    private VirtualFile findFile(@NotNull Editor editor) {
        return FileDocumentManager.getInstance().getFile(editor.getDocument());
    }
}
