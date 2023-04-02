package com.compilerexplorer.gui.tabs;

import com.compilerexplorer.common.Tabs;
import com.compilerexplorer.common.component.DataHolder;
import com.compilerexplorer.datamodel.CompiledText;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.fileTypes.PlainTextFileType;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class ExplorerStdoutTabProvider extends BaseExplorerUtilProvider {
    public ExplorerStdoutTabProvider(@NotNull Project project) {
        super(project, Tabs.EXPLORER_STDOUT, "compilerexplorer.ShowExplorerStdoutTab", PlainTextFileType.INSTANCE);
    }

    @Override
    public boolean isEnabled(@NotNull DataHolder data) {
        return compiledResult(data).map(compiledResult -> hasText(compiledResult.stdout)).orElse(false);
    }

    @Override
    public boolean isError(@NotNull DataHolder data) {
        return compiledResult(data).isEmpty();
    }

    @Override
    public void provide(@NotNull DataHolder data, @NotNull Function<String, EditorEx> textConsumer) {
        compiledResult(data).ifPresentOrElse(compiledResult -> {
            if (hasText(compiledResult.stdout)) {
                textConsumer.apply(getTextFromChunks(compiledResult.stdout));
            }
        }, () -> textConsumer.apply("Compiler Explorer was not run"));
    }
}
