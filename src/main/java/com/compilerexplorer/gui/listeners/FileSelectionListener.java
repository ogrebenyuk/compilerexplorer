package com.compilerexplorer.gui.listeners;

import com.compilerexplorer.gui.EditorGui;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.fileEditor.FileEditorManagerListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FileSelectionListener implements FileEditorManagerListener {
    @NonNls
    private static final Logger LOG = Logger.getInstance(FileSelectionListener.class);

    @NotNull
    private final Project project;

    public FileSelectionListener(@NotNull Project project_) {
        LOG.debug("created");

        project = project_;
    }

    @Override
    public void selectionChanged(@NotNull FileEditorManagerEvent event) {
        LOG.debug("selectionChanged");
        if (event.getNewFile() != null) {
            for (Editor editor : EditorFactory.getInstance().getAllEditors()) {
                VirtualFile file = FileDocumentManager.getInstance().getFile(editor.getDocument());
                if (event.getNewFile().equals(file)) {
                    updateCaretTracker(file, editor);
                }
            }
        } else if (event.getOldFile() != null) {
            updateCaretTracker(event.getOldFile(), null);
        }
    }

    private void updateCaretTracker(@NotNull VirtualFile file, @Nullable Editor editor) {
        EditorGui editorGui = project.getUserData(EditorGui.KEY);
        if (editorGui != null) {
            editorGui.updateCaretTracker(file, editor);
        }
    }
}
