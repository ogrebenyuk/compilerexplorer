package com.compilerexplorer.common;

import com.compilerexplorer.common.state.Defines;
import org.jetbrains.annotations.NotNull;

public class PreprocessableSource {
    @NotNull
    private final SourceRemoteMatched sourceRemoteMatched;
    @NotNull
    private final Defines defines;

    public PreprocessableSource(@NotNull SourceRemoteMatched sourceRemoteMatched_, @NotNull Defines defines_) {
        sourceRemoteMatched = sourceRemoteMatched_;
        defines = defines_;
    }

    @NotNull
    public SourceRemoteMatched getSourceRemoteMatched() {
        return sourceRemoteMatched;
    }

    @NotNull
    public Defines getDefines() {
        return defines;
    }
}
