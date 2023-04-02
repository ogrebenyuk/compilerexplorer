package com.compilerexplorer.gui.tabs;

import com.compilerexplorer.common.Tabs;
import com.compilerexplorer.common.component.DataHolder;
import com.compilerexplorer.datamodel.CompiledText;
import com.intellij.json.JsonFileType;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

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
        return compiledText(data).isEmpty();
    }

    @Override
    public void provide(@NotNull DataHolder data, @NotNull Function<String, EditorEx> textConsumer) {
        compiledText(data).ifPresentOrElse(
                compiledText -> textConsumer.apply(compiledText.getRawOutput()),
                () -> textConsumer.apply("Compiler Explorer was not run")
        );
    }
}
