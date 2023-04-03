package com.compilerexplorer.gui.tabs;

import com.compilerexplorer.common.CompilerExplorerSettingsProvider;
import com.compilerexplorer.common.Tabs;
import com.compilerexplorer.common.component.DataHolder;
import com.compilerexplorer.datamodel.CompiledText;
import com.compilerexplorer.datamodel.state.SettingsState;
import com.google.common.collect.ImmutableList;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.PlainTextFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

public abstract class BaseTabProvider implements TabProvider {
    @NotNull
    private static final FileType FILE_TYPE_ON_ERROR = PlainTextFileType.INSTANCE;
    @NotNull
    private static final List<TextRange> NO_RANGES = ImmutableList.of();
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

    public BaseTabProvider(@NotNull Project project_, @NotNull Tabs tab_, @NotNull String actionId_, @NotNull FileType fileType_) {
        project = project_;
        state = CompilerExplorerSettingsProvider.getInstance(project).getState();
        tab = tab_;
        actionId = actionId_;
        fileType = fileType_;
    }

    @Override
    @NotNull
    public Tabs getTab() {
        return tab;
    }

    @Override
    @NotNull
    public String actionId() {
        return actionId;
    }

    @Override
    @NotNull
    public FileType getFileType(@NotNull DataHolder data) {
        return isError(data) ? FILE_TYPE_ON_ERROR : fileType;
    }

    public void provide(@NotNull DataHolder data, @NotNull Consumer<String> textConsumer) {
        // empty
    }

    @Override
    public void provide(@NotNull DataHolder data, @NotNull BiConsumer<String, Optional<List<FoldingRegion>>> textAndFoldingConsumer) {
        provide(data, text -> textAndFoldingConsumer.accept(text, Optional.empty()));
    }

    @Override
    public void highlightLocations(@NotNull EditorEx ed, @NotNull List<CompiledText.SourceLocation> highlightedLocations) {
        // empty
    }

    @Override
    @NotNull
    public List<TextRange> getRangesForLocation(@NotNull CompiledText.SourceLocation location) {
        return NO_RANGES;
    }

    @Override
    public void editorCreated(@NotNull EditorEx ed) {
        // empty
    }

    @Override
    public void updateGutter(@NotNull EditorEx ed) {
        // empty
    }

    @Override
    public void applyThemeColors() {
        // empty
    }

    @Override
    public boolean isSourceSpecific() {
        return true;
    }

    @Override
    @NotNull
    public String defaultExtension(@NotNull DataHolder data) {
        return getFileType(data).getDefaultExtension();
    }

    @NotNull
    protected SettingsState getState() {
        return CompilerExplorerSettingsProvider.getInstance(project).getState();
    }
}
