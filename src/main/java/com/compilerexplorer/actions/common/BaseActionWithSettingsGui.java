package com.compilerexplorer.actions.common;

import com.compilerexplorer.settings.gui.SettingsGui;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public interface BaseActionWithSettingsGui extends BaseActionUtil {
    default void withSettingsGui(@NotNull AnActionEvent event, @NotNull Consumer<SettingsGui> consumer) {
        withUserData(event, SettingsGui.KEY, consumer);
    }
}
