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

    @Override
    public int hashCode() {
        return getSourceCompilerSettings().hashCode()
                + getRemoteCompilerMatches().hashCode()
                ;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof SourceRemoteMatched)) {
            return false;
        }
        SourceRemoteMatched other = (SourceRemoteMatched)obj;
        return getSourceCompilerSettings().equals(other.getSourceCompilerSettings())
                && getRemoteCompilerMatches().equals(other.getRemoteCompilerMatches())
                ;
    }
}
