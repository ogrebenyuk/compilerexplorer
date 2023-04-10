package com.compilerexplorer.gui.tabs;

import com.compilerexplorer.datamodel.state.SettingsState;
import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class TabsFactory {
    @NotNull
    public static List<TabProvider> create(@NotNull SettingsState state) {
        return ImmutableList.of(
                new ProjectInfoTabProvider(state),
                new PreprocessorVersionStdoutTabProvider(state),
                new PreprocessorVersionStderrTabProvider(state),
                new PreprocessorVersionOutputTabProvider(state),
                new PreprocessorStdoutTabProvider(state),
                new PreprocessorStderrTabProvider(state),
                new PreprocessorOutputTabProvider(state),
                new SourceInfoTabProvider(state),
                new ExplorerSiteInfoTabProvider(state),
                new ExplorerSiteRawOutputTabProvider(state),
                new ExplorerRawInputTabProvider(state),
                new ExplorerRawOutputTabProvider(state),
                new ExplorerStdoutTabProvider(state),
                new ExplorerStderrTabProvider(state),
                new ExplorerOutputTabProvider(state),
                new ExplorerOutputDevice1TabProvider(state),
                new ExplorerOutputDevice2TabProvider(state),
                new ExplorerOutputDevice3TabProvider(state),
                new ExplorerOutputDevice4TabProvider(state),
                new ExplorerOutputDevice5TabProvider(state),
                new ExplorerExecResultTabProvider(state),
                new EverythingTabProvider(state)
        );
    }
}
