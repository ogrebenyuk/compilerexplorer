package com.compilerexplorer.common;

import org.jetbrains.annotations.NotNull;

public class SourceRemoteMatched {
    @NotNull
    private final SourceCompilerSettings sourceCompilerSettings;
    @NotNull
    private final String remoteCompilerId;

    public SourceRemoteMatched(@NotNull SourceCompilerSettings sourceCompilerSettings_, @NotNull String remoteCompilerId_) {
        sourceCompilerSettings = sourceCompilerSettings_;
        remoteCompilerId = remoteCompilerId_;
    }

    @NotNull
    public SourceCompilerSettings getSourceCompilerSettings() {
        return sourceCompilerSettings;
    }

    @NotNull
    public String getRemoteCompilerId() {
        return remoteCompilerId;
    }
}
