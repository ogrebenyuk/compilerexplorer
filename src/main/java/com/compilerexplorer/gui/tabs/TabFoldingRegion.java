package com.compilerexplorer.gui.tabs;

import com.intellij.openapi.util.TextRange;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class TabFoldingRegion {
    @NotNull
    public final TextRange range;
    @NonNls
    @NotNull
    public final String label;
    @NonNls
    @NotNull
    public final String placeholderText;

    public TabFoldingRegion(@NotNull TextRange range_, @NonNls @NotNull String label_, @NonNls @NotNull String placeholderText_) {
        range = range_;
        label = label_;
        placeholderText = placeholderText_;
    }
}
