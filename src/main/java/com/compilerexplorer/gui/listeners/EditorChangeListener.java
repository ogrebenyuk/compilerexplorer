package com.compilerexplorer.gui.listeners;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class EditorChangeListener {
    public EditorChangeListener(@NotNull Project project, @NotNull Runnable consumer, @NotNull Supplier<Boolean> suppressUpdatesProducer) {
        EditorFactory.getInstance().getEventMulticaster().addDocumentListener(new DocumentListener() {
            @Override
            public void documentChanged(@NotNull DocumentEvent event) {
                if (!suppressUpdatesProducer.get() && belongsToProject(event.getDocument())) {
                    consumer.run();
                }
            }
            private boolean belongsToProject(@NotNull Document document) {
                Editor[] editors = findEditors(document);
                return editors.length != 0;
            }
            @NotNull
            private Editor[] findEditors(@NotNull Document document) {
                return EditorFactory.getInstance().getEditors(document, project);
            }
        });

    }
}
