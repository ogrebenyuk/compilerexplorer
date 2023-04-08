package com.compilerexplorer.actions.common;

import com.compilerexplorer.gui.EditorGui;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public interface BaseActionWithEditorGui extends BaseActionUtil {
    default void withEditorGui(@NotNull AnActionEvent event, @NotNull Consumer<EditorGui> consumer) {
        withUserData(event, EditorGui.KEY, consumer);
    }

    default Runnable withEditorGuiRun(@NotNull AnActionEvent event, @NotNull Consumer<EditorGui> consumer) {
        return () -> {
            @Nullable Project project = event.getProject();
            if (project != null) {
                @Nullable EditorGui editorGui = project.getUserData(EditorGui.KEY);
                if (editorGui != null) {
                    consumer.accept(editorGui);
                }
            }
        };
    }
}
