package com.compilerexplorer.gui.tabs;

import com.compilerexplorer.common.Tabs;
import com.compilerexplorer.datamodel.CompiledText;
import com.compilerexplorer.datamodel.state.SettingsState;
import com.google.common.collect.ImmutableList;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.PlainTextFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.TextRange;
import com.intellij.util.Producer;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public abstract class BaseTabProvider implements TabProvider {
    @NotNull
    private static final FileType MESSAGE_FILE_TYPE = PlainTextFileType.INSTANCE;
    @NotNull
    private static final List<TextRange> NO_RANGES = ImmutableList.of();
    @NotNull
    private final SettingsState state;
    @NotNull
    private final Tabs tab;
    @NonNls
    @NotNull
    private final String actionId;
    @NotNull
    private final FileType fileType;

    public BaseTabProvider(@NotNull SettingsState state_, @NotNull Tabs tab_, @NonNls @NotNull String actionId_, @NotNull FileType fileType_) {
        state = state_;
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
    @NonNls
    @NotNull
    public String actionId() {
        return actionId;
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
    public void editorCreated(@NotNull Project project, @NotNull EditorEx ed) {
        // empty
    }

    @Override
    public void updateGutter(@NotNull Project project, @NotNull EditorEx ed) {
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

    @NotNull
    protected SettingsState getState() {
        return state;
    }

    @NonNls
    @NotNull
    protected String defaultExtension(@NotNull FileType filetype) {
        return filetype.getDefaultExtension();
    }

    protected void contentWithFolding(@NotNull Producer<TabContent> contentProducer, @NotNull TabContentConsumer contentConsumer) {
        contentConsumer.accept(true, false, fileType, defaultExtension(fileType), contentProducer);
    }

    protected void content(boolean enabled, @NotNull Producer<String> messageProducer, @NotNull TabContentConsumer contentConsumer) {
        contentConsumer.accept(enabled, false, fileType, defaultExtension(fileType),  () -> new TabContent(messageProducer.produce()));
    }

    protected void message(@NotNull Producer<String> messageProducer, @NotNull TabContentConsumer contentConsumer) {
        message(false, false, messageProducer, contentConsumer);
    }

    protected void error(boolean enabled, Producer<String> messageProducer, @NotNull TabContentConsumer contentConsumer) {
        message(enabled, true, messageProducer, contentConsumer);
    }

    private void message(boolean enabled, boolean error, @NotNull Producer<String> messageProducer, @NotNull TabContentConsumer contentConsumer) {
        contentConsumer.accept(enabled, error, MESSAGE_FILE_TYPE, defaultExtension(MESSAGE_FILE_TYPE), () -> new TabContent(messageProducer.produce()));
    }
}
