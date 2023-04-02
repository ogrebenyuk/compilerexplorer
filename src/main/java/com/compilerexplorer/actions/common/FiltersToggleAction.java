package com.compilerexplorer.actions.common;

import com.compilerexplorer.Pipeline;
import com.compilerexplorer.datamodel.state.Filters;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;
import java.util.function.Function;

public abstract class FiltersToggleAction extends BaseToggleAction implements BaseActionWithFilters, BaseActionWithPipeline {
    public boolean isSelected(@NotNull AnActionEvent event, @NotNull Function<Filters, Boolean> getter) {
        return withFilters(event, getter, false);
    }

    public void setSelected(@NotNull AnActionEvent event, boolean selected, @NotNull BiConsumer<Filters, Boolean> setter) {
        withFilters(event, filters -> {
            setter.accept(filters, selected);
            withPipeline(event, Pipeline::compile);
        });
    }
}
