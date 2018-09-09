package com.compilerexplorer.common.datamodel;

import org.jetbrains.annotations.NotNull;

public class PreprocessedSource {
    @NotNull
    private final PreprocessableSource preprocessableSource;
    @NotNull
    private final String preprocessedText;

    public PreprocessedSource(@NotNull PreprocessableSource preprocessableSource_, @NotNull String preprocessedText_) {
        preprocessableSource = preprocessableSource_;
        preprocessedText = preprocessedText_;
    }

    @NotNull
    public PreprocessableSource getPreprocessableSource() {
        return preprocessableSource;
    }

    @NotNull
    public String getPreprocessedText() {
        return preprocessedText;
    }

    @Override
    public int hashCode() {
        return getPreprocessableSource().hashCode()
                + getPreprocessedText().hashCode()
                ;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PreprocessedSource)) {
            return false;
        }
        PreprocessedSource other = (PreprocessedSource)obj;
        return getPreprocessableSource().equals(other.getPreprocessableSource())
                && getPreprocessedText().equals(other.getPreprocessedText())
                ;
    }
}
