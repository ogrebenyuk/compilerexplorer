package com.compilerexplorer.gui.tabs;

import com.compilerexplorer.common.Tabs;
import com.compilerexplorer.datamodel.CompiledText;
import com.compilerexplorer.datamodel.state.SettingsState;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;

public class ExplorerOutputDevice1TabProvider extends BaseExplorerOutputDeviceTabProvider {
    public ExplorerOutputDevice1TabProvider(@NotNull SettingsState state) {
        super(state, Tabs.EXPLORER_OUTPUT_DEVICE_1, "compilerexplorer.ShowExplorerOutputDevice1Tab", 1);
    }
}
