package com.compilerexplorer.gui.tabs;

import com.compilerexplorer.common.Tabs;
import com.compilerexplorer.common.component.DataHolder;
import com.compilerexplorer.datamodel.RemoteCompilersOutput;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Consumer;

public class ExplorerSiteRawOutputTabProvider extends BaseExplorerSiteUtilProvider {
    public ExplorerSiteRawOutputTabProvider(@NotNull Project project) {
        super(project, Tabs.EXPLORER_SITE_RAW_OUTPUT, "compilerexplorer.ShowExplorerSiteRawOutputTab");
    }

    @Override
    public boolean isSourceSpecific() {
        return false;
    }

    @Override
    public boolean isEnabled(@NotNull DataHolder data) {
        return false;
    }

    @Override
    public boolean isError(@NotNull DataHolder data) {
        return output(data).isEmpty();
    }

    @Override
    public void provide(@NotNull DataHolder data, @NotNull Consumer<String> textConsumer) {
        output(data).ifPresentOrElse(
                textConsumer,
                () -> showError(data, textConsumer));
    }

    @NotNull
    private static Optional<String> output(@NotNull DataHolder data) {
        return data.get(RemoteCompilersOutput.KEY).flatMap(RemoteCompilersOutput::getOutput).flatMap(RemoteCompilersOutput.Output::getRawOutput);
    }
}
