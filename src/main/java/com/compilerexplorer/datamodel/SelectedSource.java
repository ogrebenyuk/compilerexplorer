package com.compilerexplorer.datamodel;

import com.intellij.openapi.util.Key;
import org.jetbrains.annotations.NotNull;

public class SelectedSource {
    public static final Key<SelectedSource> KEY = Key.create(SelectedSource.class.getName());

    @NotNull
    private final SourceSettings selectedSource;

    public SelectedSource(@NotNull SourceSettings selectedSource_) {
        selectedSource = selectedSource_;
    }

    @NotNull
    public SourceSettings getSelectedSource() {
        return selectedSource;
    }

    @Override
    public int hashCode() {
        return selectedSource.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof SelectedSource other)) {
            return false;
        }
        return selectedSource.equals(other.selectedSource);
    }
}
