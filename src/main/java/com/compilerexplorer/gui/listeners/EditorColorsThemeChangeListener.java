package com.compilerexplorer.gui.listeners;

import com.intellij.openapi.editor.colors.EditorColorsListener;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EditorColorsThemeChangeListener implements EditorColorsListener {
    @NotNull
    private final Runnable consumer;

    public EditorColorsThemeChangeListener(@NotNull Runnable consumer_) {
        consumer = consumer_;
    }

    public void globalSchemeChange(@Nullable EditorColorsScheme scheme) {
        consumer.run();
    }
}
