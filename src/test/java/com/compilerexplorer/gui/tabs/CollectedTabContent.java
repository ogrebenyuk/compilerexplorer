package com.compilerexplorer.gui.tabs;

import com.compilerexplorer.common.Bundle;
import com.compilerexplorer.common.Tabs;
import com.intellij.openapi.fileTypes.FileType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.PropertyKey;

import java.util.Objects;

public class CollectedTabContent {
    @NotNull
    public final Tabs tab;
    public final boolean enabled;
    public final boolean error;
    @NotNull
    public final FileType filetype;
    @NonNls
    @NotNull
    public final String defaultExtension;
    public final boolean folding;
    @NonNls
    @Nullable
    @PropertyKey(resourceBundle = Bundle.BUNDLE_FILE)
    public final String key;

    public CollectedTabContent(@NotNull Tabs tab_,
                               boolean enabled_,
                               boolean error_,
                               @NotNull FileType filetype_,
                               @NonNls @NotNull String defaultExtension_,
                               boolean folding_,
                               @NonNls
                               @Nullable
                               @PropertyKey(resourceBundle = Bundle.BUNDLE_FILE)
                               String key_) {
        tab = tab_;
        enabled = enabled_;
        error = error_;
        filetype = filetype_;
        defaultExtension = defaultExtension_;
        folding = folding_;
        key = key_;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof CollectedTabContent other)) {
            return false;
        }
        return tab == other.tab &&
                enabled == other.enabled &&
                error == other.error &&
                filetype.equals(other.filetype) &&
                defaultExtension.equals(other.defaultExtension) &&
                folding == other.folding &&
                Objects.equals(key, other.key);
    }

    @Override
    @NotNull
    public String toString() {
        return "CollectedTabContent{" +
                " tab=" + tab +
                " enabled=" + enabled +
                " error=" + error +
                " filetype=" + filetype.getDescription() +
                " folding=" + folding +
                " key=" + key
                + " }\n";
    }
}
