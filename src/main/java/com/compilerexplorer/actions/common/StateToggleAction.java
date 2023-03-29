package com.compilerexplorer.actions.common;

import com.compilerexplorer.datamodel.state.SettingsState;
import com.compilerexplorer.gui.ToolWindowGui;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;
import java.util.function.Function;

public abstract class StateToggleAction extends BaseToggleAction implements BaseActionWithState, BaseActionWithToolWindowGui {
    public boolean isSelected(@NotNull AnActionEvent event, @NotNull Function<SettingsState, Boolean> getter) {
        return withState(event, getter, false);
    }

    public void setSelected(@NotNull AnActionEvent event, boolean selected, @NotNull BiConsumer<SettingsState, Boolean> setter) {
        withState(event, state -> {
            setter.accept(state, selected);
            withToolWindowGui(event, ToolWindowGui::recompile);
        });
    }
}
