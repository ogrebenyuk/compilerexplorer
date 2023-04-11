package com.compilerexplorer.actions.common;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public interface BaseActionWithProject {
    default void withProject(@NotNull AnActionEvent event, @NotNull Consumer<Project> consumer) {
        @Nullable Project project = event.getProject();
        if (project != null) {
            consumer.accept(project);
        }
    }
}
