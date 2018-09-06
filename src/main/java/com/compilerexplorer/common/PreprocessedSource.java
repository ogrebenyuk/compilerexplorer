package com.compilerexplorer.common;

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
}
