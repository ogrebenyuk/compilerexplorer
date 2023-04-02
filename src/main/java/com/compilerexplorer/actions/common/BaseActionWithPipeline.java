package com.compilerexplorer.actions.common;

import com.compilerexplorer.Pipeline;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public interface BaseActionWithPipeline extends BaseActionUtil {
    default void withPipeline(@NotNull AnActionEvent event, @NotNull Consumer<Pipeline> consumer) {
        withUserData(event, Pipeline.KEY, consumer);
    }

    default void withPipeline(@NotNull Project project, @NotNull Consumer<Pipeline> consumer) {
        withUserData(project, Pipeline.KEY, consumer);
    }
}
