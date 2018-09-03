package com.compilerexplorer.common;

import org.jetbrains.annotations.NotNull;

public class PreprocessedSource {
    @NotNull
    private final SourceSettings sourceSettings;
    @NotNull
    private final String preprocessedText;

    public PreprocessedSource(@NotNull SourceSettings sourceSettings_, @NotNull String preprocessedText_) {
        sourceSettings = sourceSettings_;
        preprocessedText = preprocessedText_;
    }

    @NotNull
    public SourceSettings getSourceSettings() {
        return sourceSettings;
    }

    @NotNull
    public String getPreprocessedText() {
        return preprocessedText;
    }
}
