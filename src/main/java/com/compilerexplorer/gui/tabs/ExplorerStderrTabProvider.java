package com.compilerexplorer.gui.tabs;

import com.compilerexplorer.common.Tabs;
import com.compilerexplorer.common.component.DataHolder;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.fileTypes.PlainTextFileType;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class ExplorerStderrTabProvider extends BaseExplorerUtilProvider {
    public ExplorerStderrTabProvider(@NotNull Project project) {
        super(project, Tabs.EXPLORER_STDERR, "compilerexplorer.ShowExplorerStderrTab", PlainTextFileType.INSTANCE);
    }

    @Override
    public boolean isEnabled(@NotNull DataHolder data) {
        return compiledResult(data).map(compiledResult -> hasText(compiledResult.stderr)).orElse(false);
    }

    @Override
    public boolean isError(@NotNull DataHolder data) {
        return compiledResult(data).isEmpty();
    }

    @Override
    public void provide(@NotNull DataHolder data, @NotNull Function<String, EditorEx> textConsumer) {
        compiledResult(data).ifPresentOrElse(compiledResult -> {
            if (hasText(compiledResult.stderr)) {
                textConsumer.apply(getTextFromChunks(compiledResult.stderr));
            }
        }, () -> textConsumer.apply("Compiler Explorer was not run"));
    }
}
