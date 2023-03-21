package com.compilerexplorer.datamodel;

import com.compilerexplorer.datamodel.state.LocalCompilerSettings;
import org.jetbrains.annotations.NotNull;

public class SourceCompilerSettings {
    @NotNull
    private final SourceSettings sourceSettings;
    @NotNull
    private final LocalCompilerSettings localCompilerSettings;

    public SourceCompilerSettings(@NotNull SourceSettings sourceSettings_, @NotNull LocalCompilerSettings localCompilerSettings_) {
        sourceSettings = sourceSettings_;
        localCompilerSettings = localCompilerSettings_;
    }

    @NotNull
    public SourceSettings getSourceSettings() {
        return sourceSettings;
    }

    @NotNull
    public LocalCompilerSettings getLocalCompilerSettings() {
        return localCompilerSettings;
    }

    @Override
    public int hashCode() {
        return getSourceSettings().hashCode()
                + getLocalCompilerSettings().hashCode()
                ;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof SourceCompilerSettings other)) {
            return false;
        }
        return getSourceSettings().equals(other.getSourceSettings())
                && getLocalCompilerSettings().equals(other.getLocalCompilerSettings())
                ;
    }
}
