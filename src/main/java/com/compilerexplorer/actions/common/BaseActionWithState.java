package com.compilerexplorer.actions.common;

import com.compilerexplorer.common.CompilerExplorerSettingsProvider;
import com.compilerexplorer.datamodel.state.SettingsState;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Function;

public interface BaseActionWithState extends BaseActionWithProject {
    @NotNull
    default SettingsState getState(@NotNull Project project) {
        return CompilerExplorerSettingsProvider.getInstance(project).getState();
    }

    default void withState(@NotNull AnActionEvent event, @NotNull Consumer<SettingsState> consumer) {
        withProject(event, project -> consumer.accept(getState(project)));
    }

    default <ReturnType> ReturnType withState(@NotNull AnActionEvent event, @NotNull Function<SettingsState, ReturnType> consumer, ReturnType defaultValue) {
        return withProject(event, project -> consumer.apply(getState(project)), defaultValue);
    }

    default void withState(@NotNull Project project, @NotNull Consumer<SettingsState> consumer) {
        consumer.accept(getState(project));
    }
}
