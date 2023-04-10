package com.compilerexplorer.gui.tabs;

import com.compilerexplorer.common.Tabs;
import com.compilerexplorer.datamodel.state.SettingsState;
import org.jetbrains.annotations.NotNull;

public class ExplorerOutputDevice5TabProvider extends BaseExplorerOutputDeviceTabProvider {
    public ExplorerOutputDevice5TabProvider(@NotNull SettingsState state) {
        super(state, Tabs.EXPLORER_OUTPUT_DEVICE_5, "compilerexplorer.ShowExplorerOutputDevice5Tab", 5);
    }
}
