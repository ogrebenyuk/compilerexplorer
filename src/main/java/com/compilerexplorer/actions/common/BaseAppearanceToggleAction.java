package com.compilerexplorer.actions.common;

import com.compilerexplorer.datamodel.state.SettingsState;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;
import java.util.function.Function;

public abstract class BaseAppearanceToggleAction extends BaseToggleAction implements BaseActionWithState {
    public boolean isSelected(@NotNull AnActionEvent event, @NotNull Function<SettingsState, Boolean> getter) {
        return withState(event, getter, false);
    }

    public void setSelected(@NotNull AnActionEvent event, boolean selected, @NotNull BiConsumer<SettingsState, Boolean> setter, @NotNull Runnable runnable) {
        withState(event, state -> {
            setter.accept(state, selected);
            runnable.run();
        });
    }
}
