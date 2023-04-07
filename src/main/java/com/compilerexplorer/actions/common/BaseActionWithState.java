package com.compilerexplorer.actions.common;

import com.compilerexplorer.common.CompilerExplorerSettingsProvider;
import com.compilerexplorer.datamodel.state.SettingsState;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;
import java.util.function.Function;

public interface BaseActionWithState extends BaseActionWithProject {
    @NotNull
    default SettingsState getState(@NotNull Project project) {
        return CompilerExplorerSettingsProvider.getInstance(project).getState();
    }

    default void withState(@NotNull AnActionEvent event, @NotNull Consumer<SettingsState> consumer) {
        @Nullable Project project = event.getProject();
        if (project != null) {
            SettingsState state = CompilerExplorerSettingsProvider.getInstance(project).getState();
            consumer.accept(state);
        }
    }

    default <ReturnType> ReturnType withState(@NotNull AnActionEvent event, @NotNull Function<SettingsState, ReturnType> consumer, ReturnType defaultValue) {
        @Nullable Project project = event.getProject();
        if (project != null) {
            SettingsState state = CompilerExplorerSettingsProvider.getInstance(project).getState();
            return consumer.apply(state);
        } else {
            return defaultValue;
        }
    }
}
