package com.compilerexplorer.actions.common;

import com.compilerexplorer.datamodel.state.Filters;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Function;

public interface BaseActionWithFilters extends BaseActionWithState {
    default <ReturnType> ReturnType withFilters(@NotNull AnActionEvent event, @NotNull Function<Filters, ReturnType> consumer, ReturnType defaultValue) {
        @Nullable Project project = event.getProject();
        if (project != null) {
            Filters filters = getState(project).getFilters();
            ReturnType ret = consumer.apply(filters);
            getState(project).setFilters(filters);
            return ret;
        } else {
            return defaultValue;
        }
    }

    default void withFilters(@NotNull AnActionEvent event, @NotNull Consumer<Filters> consumer) {
        @Nullable Project project = event.getProject();
        if (project != null) {
            Filters filters = getState(project).getFilters();
            consumer.accept(filters);
            getState(project).setFilters(filters);
        }
    }
}
