package com.compilerexplorer.gui.tabs;

import com.compilerexplorer.common.Tabs;
import com.compilerexplorer.common.component.DataHolder;
import com.compilerexplorer.datamodel.RemoteCompilersOutput;
import com.compilerexplorer.datamodel.state.RemoteCompilerInfo;
import com.compilerexplorer.datamodel.state.SettingsState;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface WithRemoteCompilers extends NoSources {
    enum Status {CANCELED, EXCEPTION, NEWLY_CONNECTED, ALREADY_CONNECTED}

    @NotNull
    default DataHolder withRemoteCompilers(@NotNull DataHolder data, @NotNull Status status) {
        data.put(RemoteCompilersOutput.KEY, switch(status) {
            case CANCELED          -> new RemoteCompilersOutput("endpoint", false, true,  new RemoteCompilersOutput.Output(null, null));
            case EXCEPTION         -> new RemoteCompilersOutput("endpoint", false, false, new RemoteCompilersOutput.Output(null, new RuntimeException()));
            case NEWLY_CONNECTED   -> new RemoteCompilersOutput("endpoint", false, false, new RemoteCompilersOutput.Output("output", null));
            case ALREADY_CONNECTED -> new RemoteCompilersOutput("endpoint", true,  false, null);
        });
        return data;
    }

    @NotNull
    default DataHolder dataWithRemoteCompilers(@NotNull Status status) {
        return withRemoteCompilers(dataWithEmptySources(), status);
    }

    @NotNull
    default SettingsState withRemoteCompilers(@NotNull SettingsState state, @NotNull Status status) {
        if (status == Status.NEWLY_CONNECTED || status == Status.ALREADY_CONNECTED) {
            state.setConnected(true);
            state.setRemoteCompilers(List.of(new RemoteCompilerInfo()));
        }
        return state;
    }

    @NotNull
    default SettingsState stateWithRemoteCompilers(@NotNull Status status) {
        return withRemoteCompilers(stateWithNoSources(), status);
    }

    @NotNull
    default List<CollectedTabContent> withRemoteCompilers(@NotNull List<CollectedTabContent> result, @NotNull Status status) {
        set(result, switch (status) {
            case CANCELED ->          error(Tabs.EXPLORER_SITE_RAW_OUTPUT, DISABLED, "compilerexplorer.BaseExplorerSiteUtilProvider.Canceled");
            case EXCEPTION ->         error(Tabs.EXPLORER_SITE_RAW_OUTPUT, DISABLED, "compilerexplorer.BaseExplorerSiteUtilProvider.Exception");
            case NEWLY_CONNECTED ->   content(Tabs.EXPLORER_SITE_RAW_OUTPUT, DISABLED, JSON);
            case ALREADY_CONNECTED -> message(Tabs.EXPLORER_SITE_RAW_OUTPUT, DISABLED, "compilerexplorer.BaseExplorerSiteUtilProvider.Cached");
        });
        set(result, switch (status) {
            case CANCELED ->          error(Tabs.EXPLORER_SITE_INFO, ENABLED, "compilerexplorer.BaseExplorerSiteUtilProvider.Canceled");
            case EXCEPTION ->         error(Tabs.EXPLORER_SITE_INFO, ENABLED, "compilerexplorer.BaseExplorerSiteUtilProvider.Exception");
            case NEWLY_CONNECTED, ALREADY_CONNECTED -> content(Tabs.EXPLORER_SITE_INFO, DISABLED, JSON);
        });
        return result;
    }

    @NotNull
    default List<CollectedTabContent> resultWithRemoteCompilers(@NotNull Status status) {
        return withRemoteCompilers(resultWithNoSources(), status);
    }
}
