package com.compilerexplorer.gui.tabs;

import com.compilerexplorer.common.Tabs;
import com.compilerexplorer.common.component.DataHolder;
import com.compilerexplorer.datamodel.RemoteCompilersOutput;
import com.intellij.json.JsonFileType;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public abstract class BaseExplorerSiteUtilProvider extends BaseTabProvider {
    public BaseExplorerSiteUtilProvider(@NotNull Project project, @NotNull Tabs tab, @NotNull String actionId) {
        super(project, tab, actionId, JsonFileType.INSTANCE);
    }

    protected void showError(@NotNull DataHolder data, @NotNull Consumer<String> textConsumer) {
        data.get(RemoteCompilersOutput.KEY).ifPresentOrElse(remoteCompilersOutput -> {
            if (!remoteCompilersOutput.getCached()) {
                remoteCompilersOutput.getOutput().flatMap(RemoteCompilersOutput.Output::getException).ifPresentOrElse(
                        exception -> {
                            String errorMessage = "Error from Compiler Explorer endpoint \""
                                    + remoteCompilersOutput.getEndpoint()
                                    + "\":\n"
                                    + exception;
                            textConsumer.accept(errorMessage);
                        },
                        () -> {
                            String errorMessage;
                            if (remoteCompilersOutput.getCanceled()) {
                                errorMessage = "Compiler Explorer endpoint \""
                                        + remoteCompilersOutput.getEndpoint()
                                        + "\" query was canceled";
                            } else {
                                errorMessage = "Unknown error from Compiler Explorer endpoint \""
                                        + remoteCompilersOutput.getEndpoint()
                                        + "\"";
                            }
                            textConsumer.accept(errorMessage);
                        }
                );
            } else {
                String errorMessage = "Compiler Explorer endpoint \""
                        + remoteCompilersOutput.getEndpoint()
                        + "\" was cached";
                textConsumer.accept(errorMessage);
            }
        }, () -> {
            String errorMessage = "Compiler Explorer URL \""
                    + state.getUrl()
                    + "\" was not queried";
            textConsumer.accept(errorMessage);
        });
    }
}
