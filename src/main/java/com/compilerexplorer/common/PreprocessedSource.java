package com.compilerexplorer.common;

import org.jetbrains.annotations.NotNull;

public class PreprocessedSource {
    @NotNull
    private final String preprocessedText;

    public PreprocessedSource(@NotNull String preprocessedText_) {
        preprocessedText = preprocessedText_;
    }

    @NotNull
    public String getPreprocessedText() {
        return preprocessedText;
    }
}
