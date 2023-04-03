package com.compilerexplorer.gui.tabs;

import com.compilerexplorer.common.Tabs;
import com.compilerexplorer.common.component.DataHolder;
import com.compilerexplorer.datamodel.CompiledText;
import com.intellij.json.JsonFileType;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class ExplorerRawOutputTabProvider extends BaseExplorerUtilProvider {
    public ExplorerRawOutputTabProvider(@NotNull Project project) {
        super(project, Tabs.EXPLORER_RAW_OUTPUT, "compilerexplorer.ShowExplorerRawOutputTab", JsonFileType.INSTANCE);
    }

    @Override
    public boolean isEnabled(@NotNull DataHolder data) {
        return false;
    }

    @Override
    public boolean isError(@NotNull DataHolder data) {
        return compiledText(data).map(CompiledText::getCanceled).orElse(true);
    }

    @Override
    public void provide(@NotNull DataHolder data, @NotNull Consumer<String> textConsumer) {
        compiledText(data).ifPresentOrElse(compiledText -> {
                    if (!compiledText.getCanceled()) {
                        textConsumer.accept(compiledText.getRawOutput());
                    } else {
                        textConsumer.accept("Compiler Explorer was canceled");
                    }
                },
                () -> textConsumer.accept("Compiler Explorer was not run")
        );
    }
}
