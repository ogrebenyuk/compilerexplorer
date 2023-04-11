package com.compilerexplorer.gui.tabs;

import com.compilerexplorer.common.Bundle;
import com.compilerexplorer.common.Tabs;
import com.compilerexplorer.common.component.DataHolder;
import com.compilerexplorer.datamodel.state.SettingsState;
import com.intellij.json.JsonFileType;
import org.jetbrains.annotations.NotNull;

public class ExplorerRawOutputTabProvider extends BaseExplorerUtilProvider {
    public ExplorerRawOutputTabProvider(@NotNull SettingsState state) {
        super(state, Tabs.EXPLORER_RAW_OUTPUT, "compilerexplorer.ShowExplorerRawOutputTab", JsonFileType.INSTANCE);
    }

    @Override
    public void provide(@NotNull DataHolder data, @NotNull TabContentConsumer contentConsumer) {
        compiledText(data).ifPresentOrElse(
            compiledText -> content(false, compiledText::getRawOutput, contentConsumer),
            () -> message(() -> Bundle.get("compilerexplorer.ExplorerRawOutputTabProvider.WasNotRun"), contentConsumer)
        );
    }
}
