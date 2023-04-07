package com.compilerexplorer.actions.common;

import com.compilerexplorer.gui.EditorGui;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public interface BaseActionWithEditorGui extends BaseActionUtil {
    default void withEditorGui(@NotNull AnActionEvent event, @NotNull Consumer<EditorGui> consumer) {
        withUserData(event, EditorGui.KEY, consumer);
    }

    default Runnable withEditorGuiRun(@NotNull AnActionEvent event, @NotNull Consumer<EditorGui> consumer) {
        return () -> withUserData(event, EditorGui.KEY, consumer);
    }
}
