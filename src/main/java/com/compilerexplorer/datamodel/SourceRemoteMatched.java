package com.compilerexplorer.datamodel;

import com.compilerexplorer.datamodel.state.CompilerMatch;
import com.compilerexplorer.datamodel.state.CompilerMatches;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class SourceRemoteMatched {
    @NotNull
    public final PreprocessedSource preprocessedSource;

    public boolean cached;
    @Nullable
    public CompilerMatches remoteCompilerMatches;

    public SourceRemoteMatched(@NotNull PreprocessedSource preprocessedSource_) {
        preprocessedSource = preprocessedSource_;
    }

    public boolean isValid() {
        return remoteCompilerMatches != null && !remoteCompilerMatches.getChosenMatch().getRemoteCompilerInfo().getId().isEmpty();
    }

    @NotNull
    public SourceRemoteMatched withChosenMatch(@NotNull CompilerMatch newChosenMatch) {
        SourceRemoteMatched newSourceRemoteMatched = new SourceRemoteMatched(preprocessedSource);
        if (remoteCompilerMatches != null) {
            newSourceRemoteMatched.remoteCompilerMatches = remoteCompilerMatches.withChosenMatch(newChosenMatch);
        }
        return newSourceRemoteMatched;
    }

    @Override
    public int hashCode() {
        return preprocessedSource.hashCode()
                + (cached ? 1 : 0)
                + (remoteCompilerMatches != null ? remoteCompilerMatches.hashCode() : 0)
                ;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof SourceRemoteMatched other)) {
            return false;
        }
        return preprocessedSource.equals(other.preprocessedSource)
                && cached == other.cached
                && Objects.equals(remoteCompilerMatches, other.remoteCompilerMatches)
                ;
    }
}
