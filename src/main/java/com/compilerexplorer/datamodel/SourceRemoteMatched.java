package com.compilerexplorer.datamodel;

import com.compilerexplorer.datamodel.state.CompilerMatch;
import com.compilerexplorer.datamodel.state.CompilerMatches;
import com.intellij.openapi.util.Key;
import org.jetbrains.annotations.NotNull;

public class SourceRemoteMatched {
    public static final Key<SourceRemoteMatched> KEY = Key.create(SourceRemoteMatched.class.getName());
    public static final Key<SourceRemoteMatched> SELECTED_KEY = Key.create(SourceRemoteMatched.class.getName() + ".selected");

    private final boolean cached;
    @NotNull
    private final CompilerMatches matches;

    public SourceRemoteMatched(boolean cached_, @NotNull CompilerMatches matches_) {
        cached = cached_;
        matches = matches_;
    }

    public boolean getCached() {
        return cached;
    }

    @NotNull
    public CompilerMatches getMatches() {
        return matches;
    }

    @NotNull
    public SourceRemoteMatched withChosenMatch(@NotNull CompilerMatch newChosenMatch) {
        return new SourceRemoteMatched(cached, matches.withChosenMatch(newChosenMatch));
    }

    @Override
    public int hashCode() {
        return (cached ? 1 : 0) + matches.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof SourceRemoteMatched other)) {
            return false;
        }
        return cached == other.cached && matches.equals(other.matches);
    }
}
