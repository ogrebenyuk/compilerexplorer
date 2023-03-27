package com.compilerexplorer.datamodel;

import com.compilerexplorer.datamodel.state.CompilerMatch;
import com.compilerexplorer.datamodel.state.CompilerMatches;
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

    @NotNull
    public SourceRemoteMatched withChosenMatch(@NotNull CompilerMatch chosenMatch) {
        return new SourceRemoteMatched(sourceCompilerSettings, remoteCompilerMatches.withChosenMatch(chosenMatch));
    }

    @Override
    public int hashCode() {
        return getSourceCompilerSettings().hashCode()
                + getRemoteCompilerMatches().hashCode()
                ;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof SourceRemoteMatched other)) {
            return false;
        }
        return getSourceCompilerSettings().equals(other.getSourceCompilerSettings())
                && getRemoteCompilerMatches().equals(other.getRemoteCompilerMatches())
                ;
    }
}
