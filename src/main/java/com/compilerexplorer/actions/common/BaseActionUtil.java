package com.compilerexplorer.actions.common;

import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.function.Consumer;

interface BaseActionUtil extends BaseActionWithProject {
    default <T> void withUserData(@NotNull AnActionEvent event, @NotNull Key<T> key, @NotNull Consumer<T> consumer) {
        @Nullable Project project = event.getProject();
        if (project != null) {
            @Nullable T data = project.getUserData(key);
            if (data != null) {
                consumer.accept(data);
            }
        }
    }

    default boolean isEnabled(@NotNull AnActionEvent event) {
        return event.getPresentation().isEnabled();
    }

    default void setEnabled(@NotNull AnActionEvent event, boolean isEnabled) {
        event.getPresentation().setEnabled(isEnabled);
    }

    default void setIcon(@NotNull AnActionEvent event, @Nullable Icon icon) {
        event.getPresentation().setIcon(icon);
    }

    default boolean isVisible(@NotNull AnActionEvent event) {
        return event.getPresentation().isVisible();
    }

    default void setVisible(@NotNull AnActionEvent event, boolean isVisible) {
        event.getPresentation().setVisible(isVisible);
    }

    default void setEnabledAndVisible(@NotNull AnActionEvent event, boolean isEnabledAndVisible) {
        event.getPresentation().setEnabledAndVisible(isEnabledAndVisible);
    }
}
