package com.compilerexplorer.gui.tabs;

import com.compilerexplorer.common.Tabs;
import com.compilerexplorer.datamodel.state.SettingsState;
import org.jetbrains.annotations.NotNull;

public class ExplorerOutputDevice4TabProvider extends BaseExplorerOutputDeviceTabProvider {
    public ExplorerOutputDevice4TabProvider(@NotNull SettingsState state) {
        super(state, Tabs.EXPLORER_OUTPUT_DEVICE_4, "compilerexplorer.ShowExplorerOutputDevice4Tab", 4);
    }
}
