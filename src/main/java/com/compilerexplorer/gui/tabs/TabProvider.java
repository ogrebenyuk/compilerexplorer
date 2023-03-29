package com.compilerexplorer.gui.tabs;

import com.compilerexplorer.common.CompilerExplorerSettingsProvider;
import com.compilerexplorer.common.Tabs;
import com.compilerexplorer.datamodel.CompiledText;
import com.compilerexplorer.datamodel.state.SettingsState;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.PlainTextFileType;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public abstract class TabProvider {
    public static class Range {
        public final int begin;
        public final int end;

        Range(int begin_, int end_) {
            begin = begin_;
            end = end_;
        }
    }

    @NotNull
    private static final FileType FILE_TYPE_ON_ERROR = PlainTextFileType.INSTANCE;

    @NotNull
    protected final Project project;
    @NotNull
    protected final SettingsState state;
    @NotNull
    private final Tabs tab;
    @NotNull
    private final String actionId;
    @NotNull
    private final FileType fileType;

    public TabProvider(@NotNull Project project_, @NotNull Tabs tab_, @NotNull String actionId_, @NotNull FileType fileType_) {
        project = project_;
        state = CompilerExplorerSettingsProvider.getInstance(project).getState();
        tab = tab_;
        actionId = actionId_;
        fileType = fileType_;
    }

    @NotNull
    public Tabs getTab() {
        return tab;
    }

    @NotNull
    public String actionId() {
        return actionId;
    }

    @NotNull
    public FileType getFileType(@NotNull CompiledText compiledText) {
        return isError(compiledText) ? FILE_TYPE_ON_ERROR : fileType;
    }

    public abstract boolean isEnabled(@NotNull CompiledText compiledText);

    public abstract boolean isError(@NotNull CompiledText compiledText);

    public abstract void provide(@NotNull CompiledText compiledText, @NotNull Function<String, EditorEx> textConsumer);

    public void highlightLocations(@NotNull EditorEx ed, @NotNull List<CompiledText.SourceLocation> highlightedLocations) {
        // empty
    }

    @Nullable
    public List<Range> getRangesForLocation(@NotNull CompiledText.SourceLocation location) {
        return null;
    }

    public void updateGutter(@NotNull EditorEx ed) {
        // empty
    }

    public void updateFolding(@NotNull EditorEx ed) {
        // empty
    }

    public void applyThemeColors() {
        // empty
    }

    @NotNull
    protected SettingsState getState() {
        return CompilerExplorerSettingsProvider.getInstance(project).getState();
    }

    protected void provideAndGetEditor(@NotNull Function<String, EditorEx> textConsumer, @NotNull String text, @NotNull Consumer<EditorEx> editorConsumer) {
        @Nullable EditorEx ed = textConsumer.apply(text);
        if (ed != null) {
            editorConsumer.accept(ed);
        }
    }
}
