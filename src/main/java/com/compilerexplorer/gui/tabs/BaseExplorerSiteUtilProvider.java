package com.compilerexplorer.gui.tabs;

import com.compilerexplorer.common.Bundle;
import com.compilerexplorer.common.Tabs;
import com.compilerexplorer.common.component.DataHolder;
import com.compilerexplorer.datamodel.RemoteCompilersOutput;
import com.compilerexplorer.datamodel.state.SettingsState;
import com.intellij.json.JsonFileType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public abstract class BaseExplorerSiteUtilProvider extends BaseTabProvider {
    public BaseExplorerSiteUtilProvider(@NotNull SettingsState state, @NotNull Tabs tab, @NonNls @NotNull String actionId) {
        super(state, tab, actionId, JsonFileType.INSTANCE);
    }

    protected void showError(boolean enabled, @NotNull DataHolder data, @NotNull TabContentConsumer contentConsumer) {
        data.get(RemoteCompilersOutput.KEY).ifPresentOrElse(remoteCompilersOutput -> {
            if (!remoteCompilersOutput.getCached()) {
                remoteCompilersOutput.getOutput().flatMap(RemoteCompilersOutput.Output::getException).ifPresentOrElse(
                    exception -> error(enabled, () -> Bundle.format("compilerexplorer.BaseExplorerSiteUtilProvider.Exception", "Endpoint", remoteCompilersOutput.getEndpoint(), "Exception", exception.getMessage()), contentConsumer),
                    () -> {
                        if (remoteCompilersOutput.getCanceled()) {
                            error(enabled, () -> Bundle.format("compilerexplorer.BaseExplorerSiteUtilProvider.Canceled", "Endpoint", remoteCompilersOutput.getEndpoint()), contentConsumer);
                        }
                    }
                );
            } else {
                message(() -> Bundle.format("compilerexplorer.BaseExplorerSiteUtilProvider.Cached", "Endpoint", remoteCompilersOutput.getEndpoint()), contentConsumer);
            }
        }, () -> message(() -> Bundle.format("compilerexplorer.BaseExplorerSiteUtilProvider.NotQueried", "Url", getState().getUrl()), contentConsumer));
    }
}
