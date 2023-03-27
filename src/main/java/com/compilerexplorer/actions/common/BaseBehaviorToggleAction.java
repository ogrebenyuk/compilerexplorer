package com.compilerexplorer.actions.common;

import com.compilerexplorer.datamodel.state.SettingsState;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;

public abstract class BaseBehaviorToggleAction extends BaseAppearanceToggleAction {
    @NotNull
    private static final Runnable EMPTY_RUNNABLE = () -> {};

    public void setSelected(@NotNull AnActionEvent event, boolean selected, @NotNull BiConsumer<SettingsState, Boolean> setter) {
        super.setSelected(event, selected, setter, EMPTY_RUNNABLE);
    }
}
