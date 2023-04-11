package com.compilerexplorer.actions.appearance.tabs;

import com.compilerexplorer.common.Bundle;
import com.compilerexplorer.common.Tabs;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class BaseShowExplorerOutputDeviceTab extends BaseShowTab {
    public BaseShowExplorerOutputDeviceTab(@NotNull Tabs tab) {
        super(tab);
    }

    @Override
    public void update(@NotNull final AnActionEvent event) {
        super.update(event);
        withEditorGui(event, editorGui -> {
            @Nullable String deviceName = editorGui.findDeviceName(getTab());
            setEnabledAndVisible(event, isEnabledAndVisible(event) && deviceName != null);
            if (getTemplateText() != null && deviceName != null) {
                event.getPresentation().setText(Bundle.format("compilerexplorer.BaseShowExplorerOutputDeviceTab.Text", "Device", deviceName));
            }
            if (getTemplatePresentation().getDescription() != null && deviceName != null) {
                event.getPresentation().setDescription(Bundle.format("compilerexplorer.BaseShowExplorerOutputDeviceTab.Description", "Device", deviceName));
            }
        });
    }
}
