package com.compilerexplorer.actions.common;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Function;

public interface BaseActionWithProject {
    default void withProject(@NotNull AnActionEvent event, @NotNull Consumer<Project> consumer) {
        @Nullable Project project = event.getProject();
        if (project != null) {
            consumer.accept(project);
        }
    }

    default <ReturnType> ReturnType withProject(@NotNull AnActionEvent event, @NotNull Function<Project, ReturnType> consumer, ReturnType defaultValue) {
        @Nullable Project project = event.getProject();
        if (project != null) {
            return consumer.apply(project);
        } else {
            return defaultValue;
        }
    }
}
