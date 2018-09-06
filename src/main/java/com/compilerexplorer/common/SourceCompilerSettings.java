package com.compilerexplorer.common;

import org.jetbrains.annotations.NotNull;

public class SourceCompilerSettings {
    @NotNull
    private final SourceSettings sourceSettings;
    @NotNull
    private final SettingsState.LocalCompilerSettings localCompilerSettings;

    public SourceCompilerSettings(@NotNull SourceSettings sourceSettings_, @NotNull SettingsState.LocalCompilerSettings localCompilerSettings_) {
        sourceSettings = sourceSettings_;
        localCompilerSettings = localCompilerSettings_;
    }

    @NotNull
    public SourceSettings getSourceSettings() {
        return sourceSettings;
    }

    @NotNull
    public SettingsState.LocalCompilerSettings getLocalCompilerSettings() {
        return localCompilerSettings;
    }
}
