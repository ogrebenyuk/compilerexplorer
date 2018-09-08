package com.compilerexplorer.common;

import com.compilerexplorer.common.state.CompilerMatches;
import com.compilerexplorer.common.state.RemoteCompilerId;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SourceRemoteMatched {
    @NotNull
    private final SourceCompilerSettings sourceCompilerSettings;
    @NotNull
    private final CompilerMatches remoteCompilerMatches;

    public SourceRemoteMatched(@NotNull SourceCompilerSettings sourceCompilerSettings_, @NotNull CompilerMatches remoteCompilerMatches_) {
        sourceCompilerSettings = sourceCompilerSettings_;
        remoteCompilerMatches = remoteCompilerMatches_;
    }

    @NotNull
    public SourceCompilerSettings getSourceCompilerSettings() {
        return sourceCompilerSettings;
    }

    @NotNull
    public CompilerMatches getRemoteCompilerMatches() {
        return remoteCompilerMatches;
    }
}
