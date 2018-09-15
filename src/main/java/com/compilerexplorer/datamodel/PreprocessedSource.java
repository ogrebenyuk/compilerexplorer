package com.compilerexplorer.datamodel;

import org.jetbrains.annotations.NotNull;

public class PreprocessedSource {
    @NotNull
    private final SourceRemoteMatched sourceRemoteMatched;
    @NotNull
    private final String preprocessedText;

    public PreprocessedSource(@NotNull SourceRemoteMatched sourceRemoteMatched_, @NotNull String preprocessedText_) {
        sourceRemoteMatched = sourceRemoteMatched_;
        preprocessedText = preprocessedText_;
    }

    @NotNull
    public SourceRemoteMatched getSourceRemoteMatched() {
        return sourceRemoteMatched;
    }

    @NotNull
    public String getPreprocessedText() {
        return preprocessedText;
    }

    @Override
    public int hashCode() {
        return getSourceRemoteMatched().hashCode()
                + getPreprocessedText().hashCode()
                ;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PreprocessedSource)) {
            return false;
        }
        PreprocessedSource other = (PreprocessedSource)obj;
        return getSourceRemoteMatched().equals(other.getSourceRemoteMatched())
                && getPreprocessedText().equals(other.getPreprocessedText())
                ;
    }
}
