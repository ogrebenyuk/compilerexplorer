package com.compilerexplorer.actions.common;

import com.compilerexplorer.datamodel.state.Filters;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Function;

public interface BaseActionWithFilters extends BaseActionWithState {
    default <ReturnType> ReturnType withFilters(@NotNull AnActionEvent event, @NotNull Function<Filters, ReturnType> consumer, ReturnType defaultValue) {
        return withProject(event, project -> {
            Filters filters = getState(project).getFilters();
            ReturnType ret = consumer.apply(filters);
            getState(project).setFilters(filters);
            return ret;
        }, defaultValue);
    }

    default void withFilters(@NotNull AnActionEvent event, @NotNull Consumer<Filters> consumer) {
        withProject(event, project -> {
            Filters filters = getState(project).getFilters();
            consumer.accept(filters);
            getState(project).setFilters(filters);
        });
    }
}
