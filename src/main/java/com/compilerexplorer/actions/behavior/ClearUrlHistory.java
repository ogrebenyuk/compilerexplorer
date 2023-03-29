package com.compilerexplorer.actions.behavior;

import com.compilerexplorer.actions.common.BaseAction;
import com.compilerexplorer.actions.common.BaseActionWithSettingsGui;
import com.compilerexplorer.actions.common.BaseActionWithState;
import com.compilerexplorer.datamodel.state.SettingsState;
import com.compilerexplorer.settings.gui.SettingsGui;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class ClearUrlHistory extends BaseAction implements BaseActionWithState, BaseActionWithSettingsGui {
    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        withState(event, SettingsState::clearUrlHistory);
        withSettingsGui(event, SettingsGui::refreshUrlHistoryInGui);
    }
}
