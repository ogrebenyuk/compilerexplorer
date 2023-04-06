package com.compilerexplorer.gui.tabs;

import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Optional;

public class TabContent {
    @Nls
    @NotNull
    private final String content;
    @Nullable
    private final List<TabFoldingRegion> folding;

    public TabContent(@Nls @NotNull String content_) {
        content = content_;
        folding = null;
    }

    public TabContent(@Nls @NotNull String content_, @Nullable List<TabFoldingRegion> folding_) {
        content = content_;
        folding = folding_;
    }

    @Nls
    @NotNull
    public String getContent() {
        return content;
    }

    @NotNull
    public Optional<List<TabFoldingRegion>> getFolding() {
        return Optional.ofNullable(folding);
    }
}
