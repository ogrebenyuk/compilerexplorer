package com.compilerexplorer.gui.tabs;

import com.compilerexplorer.common.Bundle;
import com.compilerexplorer.common.Tabs;
import com.compilerexplorer.common.component.DataHolder;
import com.intellij.json.JsonFileType;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class ExplorerRawInputTabProvider extends BaseExplorerUtilProvider {
    public ExplorerRawInputTabProvider(@NotNull Project project) {
        super(project, Tabs.EXPLORER_RAW_INPUT, "compilerexplorer.ShowExplorerRawInputTab", JsonFileType.INSTANCE);
    }

    @Override
    public boolean isEnabled(@NotNull DataHolder data) {
        return false;
    }

    @Override
    public boolean isError(@NotNull DataHolder data) {
        return compiledText(data).isEmpty();
    }

    @Override
    public void provide(@NotNull DataHolder data, @NotNull Consumer<String> textConsumer) {
        compiledText(data).ifPresentOrElse(
                compiledText -> textConsumer.accept(compiledText.getRawInput()),
                () -> textConsumer.accept(Bundle.get("compilerexplorer.ExplorerRawInputTabProvider.WasNotRun"))
        );
    }
}
