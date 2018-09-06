package com.compilerexplorer.common;

import org.jetbrains.annotations.NotNull;

public class PreprocessableSource {
    @NotNull
    private final SourceRemoteMatched sourceRemoteMatched;
    @NotNull
    private final String defines;

    public PreprocessableSource(@NotNull SourceRemoteMatched sourceRemoteMatched_, @NotNull String defines_) {
        sourceRemoteMatched = sourceRemoteMatched_;
        defines = defines_;
    }

    @NotNull
    public SourceRemoteMatched getSourceRemoteMatched() {
        return sourceRemoteMatched;
    }

    @NotNull
    public String getDefines() {
        return defines;
    }
}
