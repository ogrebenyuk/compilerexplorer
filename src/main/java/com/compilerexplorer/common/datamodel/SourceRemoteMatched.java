package com.compilerexplorer.common.datamodel;

import com.compilerexplorer.common.datamodel.state.CompilerMatches;
import org.jetbrains.annotations.NotNull;

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
