package com.compilerexplorer.gui.listeners;

import com.compilerexplorer.gui.EditorGui;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.colors.EditorColorsListener;
import com.intellij.openapi.editor.colors.EditorColorsScheme;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class EditorColorsThemeChangeListener implements EditorColorsListener {
    private static final Logger LOG = Logger.getInstance(EditorColorsThemeChangeListener.class);

    @NotNull
    private final Project project;

    public EditorColorsThemeChangeListener(@NotNull Project project_) {
        LOG.debug("created");

        project = project_;
    }

    public void globalSchemeChange(@Nullable EditorColorsScheme scheme) {
        LOG.debug("globalSchemeChange");
        EditorGui editorGui = project.getUserData(EditorGui.KEY);
        if (editorGui != null) {
            editorGui.applyThemeColors();
        }
    }
}
