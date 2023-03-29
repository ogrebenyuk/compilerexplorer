package com.compilerexplorer.gui;

import com.intellij.openapi.editor.EditorGutterAction;
import com.intellij.openapi.editor.TextAnnotationGutterProvider;
import com.intellij.openapi.editor.ex.EditorEx;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.function.Consumer;

public class SourceAnnotationGutterAction implements EditorGutterAction {
    @NotNull
    private final TextAnnotationGutterProvider provider;
    @NotNull
    private final EditorEx editor;
    @NotNull
    private final Consumer<Integer> clickConsumer;

    public SourceAnnotationGutterAction(@NotNull TextAnnotationGutterProvider provider_, @NotNull EditorEx editor_, @NotNull Consumer<Integer> clickConsumer_) {
        provider = provider_;
        editor = editor_;
        clickConsumer = clickConsumer_;
    }

    @Override
    public void doAction(int line) {
        if (provider.getLineText(line, editor) != null) {
            clickConsumer.accept(line);
        }
    }

    @Override
    @NotNull
    public Cursor getCursor(int line) {
        Cursor[] cursors = new Cursor[]{Cursor.getPredefinedCursor(Cursor.DEFAULT_CURSOR)};
        if (provider.getLineText(line, editor) != null) {
            cursors[0] = Cursor.getPredefinedCursor(Cursor.HAND_CURSOR);
        }
        return cursors[0];
    }
}

