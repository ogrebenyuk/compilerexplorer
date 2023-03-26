package com.compilerexplorer.gui.listeners;

import com.intellij.openapi.editor.FoldRegion;
import com.intellij.openapi.editor.ex.FoldingListener;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiConsumer;

public class FoldingChangeListener implements FoldingListener {
    @NotNull
    private final BiConsumer<String, Boolean> consumer;

    public FoldingChangeListener(@NotNull BiConsumer<String, Boolean> consumer_) {
        consumer = consumer_;
    }

    @Override
    public void onFoldRegionStateChange(@NotNull FoldRegion region) {
        FoldingListener.super.onFoldRegionStateChange(region);
        consumer.accept(region.getPlaceholderText(), region.isExpanded());
    }
}
