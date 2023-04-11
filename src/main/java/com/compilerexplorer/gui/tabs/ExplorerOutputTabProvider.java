package com.compilerexplorer.gui.tabs;

import com.compilerexplorer.common.*;
import com.compilerexplorer.datamodel.state.SettingsState;
import org.jetbrains.annotations.NotNull;

public class ExplorerOutputTabProvider extends BaseExplorerOutputTabProvider {
    public ExplorerOutputTabProvider(@NotNull SettingsState state) {
        super(state, Tabs.EXPLORER_OUTPUT, "compilerexplorer.ShowExplorerOutputTab", compilerResult -> compilerResult);
    }
}
