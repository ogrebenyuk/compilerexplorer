package com.compilerexplorer.gui.tabs;

import com.compilerexplorer.common.Tabs;
import com.compilerexplorer.common.component.DataHolder;
import com.compilerexplorer.datamodel.RemoteCompilersOutput;
import com.intellij.json.JsonFileType;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public abstract class BaseExplorerSiteUtilProvider extends BaseTabProvider {
    public BaseExplorerSiteUtilProvider(@NotNull Project project, @NotNull Tabs tab, @NotNull String actionId) {
        super(project, tab, actionId, JsonFileType.INSTANCE);
    }

    protected void showError(@NotNull DataHolder data, @NotNull Function<String, EditorEx> textConsumer) {
        data.get(RemoteCompilersOutput.KEY).ifPresentOrElse(remoteCompilersOutput -> {
            if (!remoteCompilersOutput.getCached()) {
                remoteCompilersOutput.getOutput().flatMap(RemoteCompilersOutput.Output::getException).ifPresentOrElse(
                        exception -> {
                            String errorMessage = "Error from Compiler Explorer endpoint \""
                                    + remoteCompilersOutput.getEndpoint()
                                    + "\":\n"
                                    + exception;
                            textConsumer.apply(errorMessage);
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
                            textConsumer.apply(errorMessage);
                        }
                );
            } else {
                String errorMessage = "Compiler Explorer endpoint \""
                        + remoteCompilersOutput.getEndpoint()
                        + "\" was cached";
                textConsumer.apply(errorMessage);
            }
        }, () -> {
            String errorMessage = "Compiler Explorer URL \""
                    + state.getUrl()
                    + "\" was not queried";
            textConsumer.apply(errorMessage);
        });
    }
}
