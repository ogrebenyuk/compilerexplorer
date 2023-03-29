package com.compilerexplorer.gui.listeners;

import com.compilerexplorer.common.DisposableParentProjectService;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.project.Project;
import com.intellij.util.Producer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;
import java.util.Optional;

public class EditorChangeListener {
    @NotNull
    private final Producer<EditorEx> excludedEditorProducer;

    public EditorChangeListener(@NotNull Project project, @NotNull Producer<EditorEx> excludedEditorProducer_, @NotNull Runnable consumer) {
        excludedEditorProducer = excludedEditorProducer_;

        EditorFactory.getInstance().getEventMulticaster().addDocumentListener(new DocumentListener() {
            @Override
            public void documentChanged(@NotNull DocumentEvent event) {
                if (belongsToProject(event.getDocument())) {
                    consumer.run();
                }
            }
            private boolean belongsToProject(@NotNull Document document) {
                Optional<Editor> anyEditor = findAnyEditor(document);
                return anyEditor.isPresent();
            }
            @NotNull
            private Optional<Editor> findAnyEditor(@NotNull Document document) {
                @Nullable EditorEx excludedEditor = excludedEditorProducer.produce();
                return Arrays.stream(EditorFactory.getInstance().getEditors(document, project))
                        .filter(ed -> ed != excludedEditor)
                        .findFirst();
            }
        }, DisposableParentProjectService.getInstance(project));

    }
}
