package com.compilerexplorer.gui.tabs;

import com.compilerexplorer.common.Bundle;
import com.compilerexplorer.common.Tabs;
import com.compilerexplorer.common.component.DataHolder;
import com.compilerexplorer.datamodel.RemoteCompilersOutput;
import com.intellij.json.JsonFileType;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public abstract class BaseExplorerSiteUtilProvider extends BaseTabProvider {
    public BaseExplorerSiteUtilProvider(@NotNull Project project, @NotNull Tabs tab, @NonNls @NotNull String actionId) {
        super(project, tab, actionId, JsonFileType.INSTANCE);
    }

    protected void showError(@NotNull DataHolder data, @NotNull Consumer<String> textConsumer) {
        data.get(RemoteCompilersOutput.KEY).ifPresentOrElse(remoteCompilersOutput -> {
            if (!remoteCompilersOutput.getCached()) {
                remoteCompilersOutput.getOutput().flatMap(RemoteCompilersOutput.Output::getException).ifPresentOrElse(
                        exception -> textConsumer.accept(Bundle.format("compilerexplorer.BaseExplorerSiteUtilProvider.Exception", "Endpoint", remoteCompilersOutput.getEndpoint(), "Exception", exception.getMessage())),
                        () -> {
                            if (remoteCompilersOutput.getCanceled()) {
                                textConsumer.accept(Bundle.format("compilerexplorer.BaseExplorerSiteUtilProvider.Canceled", "Endpoint", remoteCompilersOutput.getEndpoint()));
                            } else {
                                textConsumer.accept(Bundle.format("compilerexplorer.BaseExplorerSiteUtilProvider.UnknownError", "Endpoint", remoteCompilersOutput.getEndpoint()));
                            }
                        }
                );
            } else {
                textConsumer.accept(Bundle.format("compilerexplorer.BaseExplorerSiteUtilProvider.Cached", "Endpoint", remoteCompilersOutput.getEndpoint()));
            }
        }, () -> textConsumer.accept(Bundle.format("compilerexplorer.BaseExplorerSiteUtilProvider.NotQueried", "Url", state.getUrl())));
    }
}
