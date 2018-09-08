package com.compilerexplorer.common;

import com.compilerexplorer.common.state.LocalCompilerSettings;
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
}
