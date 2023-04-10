package com.compilerexplorer.gui.tabs;

import com.compilerexplorer.common.Tabs;
import com.compilerexplorer.datamodel.state.SettingsState;
import org.jetbrains.annotations.NotNull;

public class ExplorerOutputDevice3TabProvider extends BaseExplorerOutputDeviceTabProvider {
    public ExplorerOutputDevice3TabProvider(@NotNull SettingsState state) {
        super(state, Tabs.EXPLORER_OUTPUT_DEVICE_3, "compilerexplorer.ShowExplorerOutputDevice3Tab", 3);
    }
}
