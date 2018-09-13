package com.compilerexplorer.gui.listeners;

import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.event.DocumentEvent;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.project.Project;
import com.intellij.util.Producer;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class EditorChangeListener {
    public EditorChangeListener(@NotNull Project project, @NotNull Consumer<Boolean> consumer, @NotNull Producer<Boolean> suppressUpdatesProducer) {
        EditorFactory.getInstance().getEventMulticaster().addDocumentListener(new DocumentListener() {
            @Override
            public void documentChanged(DocumentEvent event) {
                if (!suppressUpdatesProducer.produce() && belongsToProject(event.getDocument())) {
                    consumer.accept(false);
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
