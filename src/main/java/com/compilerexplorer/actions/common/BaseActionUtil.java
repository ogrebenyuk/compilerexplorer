package com.compilerexplorer.actions.common;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

interface BaseActionUtil extends BaseActionWithProject {
    default <T> void withUserData(@NotNull AnActionEvent event, @NotNull Key<T> key, @NotNull Consumer<T> consumer) {
        withProject(event, project -> withUserData(project, key, consumer));
    }

    default <T> void withUserData(@NotNull Project project, @NotNull Key<T> key, @NotNull Consumer<T> consumer) {
        @Nullable T data = project.getUserData(key);
        if (data != null) {
            consumer.accept(data);
        }
    }

    default boolean isEnabled(@NotNull AnActionEvent event) {
        return event.getPresentation().isEnabled();
    }

    default void setEnabled(@NotNull AnActionEvent event, boolean isEnabled) {
        event.getPresentation().setEnabled(isEnabled);
    }

    default boolean isVisible(@NotNull AnActionEvent event) {
        return event.getPresentation().isVisible();
    }

    default void setVisible(@NotNull AnActionEvent event, boolean isVisible) {
        event.getPresentation().setVisible(isVisible);
    }

    default boolean isEnabledAndVisible(@NotNull AnActionEvent event) {
        return event.getPresentation().isEnabledAndVisible();
    }

    default void setEnabledAndVisible(@NotNull AnActionEvent event, boolean isEnabledAndVisible) {
        event.getPresentation().setEnabledAndVisible(isEnabledAndVisible);
    }
}
