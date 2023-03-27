package com.compilerexplorer.actions.common;

import com.compilerexplorer.gui.ToolWindowGui;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public interface BaseActionWithToolWindowGui extends BaseActionUtil {
    default void withToolWindowGui(@NotNull AnActionEvent event, @NotNull Consumer<ToolWindowGui> consumer) {
        withUserData(event, ToolWindowGui.KEY, consumer);
    }

    default void withToolWindowGui(@NotNull Project project, @NotNull Consumer<ToolWindowGui> consumer) {
        withUserData(project, ToolWindowGui.KEY, consumer);
    }
}
