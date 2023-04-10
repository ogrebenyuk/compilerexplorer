package com.compilerexplorer.gui.tabs;

import com.compilerexplorer.common.Tabs;
import com.compilerexplorer.datamodel.state.SettingsState;
import org.jetbrains.annotations.NotNull;

public class ExplorerOutputDevice2TabProvider extends BaseExplorerOutputDeviceTabProvider {
    public ExplorerOutputDevice2TabProvider(@NotNull SettingsState state) {
        super(state, Tabs.EXPLORER_OUTPUT_DEVICE_2, "compilerexplorer.ShowExplorerOutputDevice2Tab", 2);
    }
}
