package com.compilerexplorer.gui.tabs;

import com.compilerexplorer.common.Bundle;
import com.compilerexplorer.common.Tabs;
import com.compilerexplorer.common.component.DataHolder;
import com.compilerexplorer.datamodel.CompiledText;
import com.compilerexplorer.datamodel.state.SettingsState;
import com.intellij.openapi.fileTypes.PlainTextFileType;
import org.jetbrains.annotations.NotNull;

public class ExplorerStdoutTabProvider extends BaseExplorerUtilProvider {
    public ExplorerStdoutTabProvider(@NotNull SettingsState state) {
        super(state, Tabs.EXPLORER_STDOUT, "compilerexplorer.ShowExplorerStdoutTab", PlainTextFileType.INSTANCE);
    }

    @Override
    public void provide(@NotNull DataHolder data, @NotNull TabContentConsumer contentConsumer) {
        compiledText(data).ifPresentOrElse(
                compiledText -> {
                    CompiledText.CompiledResult result = compiledText.getCompiledResultIfGood().orElse(null);
                    content(result == null, () -> result != null ? getTextFromChunks(result.stdout) : "", contentConsumer);
                },
                () -> message(false, () -> Bundle.get("compilerexplorer.ExplorerStdoutTabProvider.WasNotRun"), contentConsumer)
        );
    }
}
