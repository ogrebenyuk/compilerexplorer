package com.compilerexplorer.common;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SourceRemoteMatched {
    @NotNull
    private final SourceCompilerSettings sourceCompilerSettings;
    @NotNull
    private final List<String> remoteCompilerIds;

    public SourceRemoteMatched(@NotNull SourceCompilerSettings sourceCompilerSettings_, @NotNull List<String> remoteCompilerIds_) {
        sourceCompilerSettings = sourceCompilerSettings_;
        remoteCompilerIds = remoteCompilerIds_;
    }

    @NotNull
    public SourceCompilerSettings getSourceCompilerSettings() {
        return sourceCompilerSettings;
    }

    @NotNull
    public List<String> getRemoteCompilerIds() {
        return remoteCompilerIds;
    }
}
