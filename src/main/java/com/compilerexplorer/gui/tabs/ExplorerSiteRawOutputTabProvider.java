package com.compilerexplorer.gui.tabs;

import com.compilerexplorer.common.Tabs;
import com.compilerexplorer.common.component.DataHolder;
import com.compilerexplorer.datamodel.RemoteCompilersOutput;
import com.compilerexplorer.datamodel.state.SettingsState;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ExplorerSiteRawOutputTabProvider extends BaseExplorerSiteUtilProvider {
    public ExplorerSiteRawOutputTabProvider(@NotNull SettingsState state) {
        super(state, Tabs.EXPLORER_SITE_RAW_OUTPUT, "compilerexplorer.ShowExplorerSiteRawOutputTab");
    }

    @Override
    public boolean isSourceSpecific() {
        return false;
    }

    @Override
    public void provide(@NotNull DataHolder data, @NotNull TabContentConsumer contentConsumer) {
        output(data).ifPresentOrElse(
                output -> content(false, () -> output, contentConsumer),
                () -> showError(false, data, contentConsumer));
    }

    @NotNull
    private static Optional<String> output(@NotNull DataHolder data) {
        return data.get(RemoteCompilersOutput.KEY).flatMap(RemoteCompilersOutput::getOutput).flatMap(RemoteCompilersOutput.Output::getRawOutput);
    }
}
