package com.compilerexplorer.actions.behavior;

import com.compilerexplorer.actions.common.BaseAction;
import com.compilerexplorer.actions.common.BaseActionWithSettingsGui;
import com.compilerexplorer.settings.gui.SettingsGui;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class TestConnection extends BaseAction implements BaseActionWithSettingsGui {
    @Override
    public void actionPerformed(@NotNull AnActionEvent event) {
        withSettingsGui(event, SettingsGui::testConnection);
    }

    @Override
    public void update(@NotNull final AnActionEvent event) {
        super.update(event);
        withProject(event, project -> setEnabledAndVisible(event, isVisible(event) && SettingsGui.isSettingsGuiActive(project)));
    }
}
