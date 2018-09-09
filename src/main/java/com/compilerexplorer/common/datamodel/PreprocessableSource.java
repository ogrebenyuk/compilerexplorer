package com.compilerexplorer.common.datamodel;

import com.compilerexplorer.common.datamodel.state.Defines;
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

    @Override
    public int hashCode() {
        return getSourceRemoteMatched().hashCode()
                + getDefines().hashCode()
                ;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PreprocessableSource)) {
            return false;
        }
        PreprocessableSource other = (PreprocessableSource)obj;
        return getSourceRemoteMatched().equals(other.getSourceRemoteMatched())
                && getDefines().equals(other.getDefines())
                ;
    }
}
