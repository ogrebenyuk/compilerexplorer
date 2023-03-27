package com.compilerexplorer.actions.common;

import com.compilerexplorer.datamodel.state.Filters;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Function;

public interface BaseActionWithFilters extends BaseActionWithState {
    default <ReturnType> ReturnType withFilters(@NotNull AnActionEvent event, @NotNull Function<Filters, ReturnType> consumer, ReturnType defaultValue) {
        return withProject(event, project -> consumer.apply(getState(project).getFilters()), defaultValue);
    }

    default void withFilters(@NotNull AnActionEvent event, @NotNull Consumer<Filters> consumer) {
        withProject(event, project -> consumer.accept(getState(project).getFilters()));
    }

    default void withFilters(@NotNull Project project, @NotNull Consumer<Filters> consumer) {
        consumer.accept(getState(project).getFilters());
    }
}
