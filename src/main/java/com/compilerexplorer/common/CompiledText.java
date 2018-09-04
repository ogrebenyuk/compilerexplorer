package com.compilerexplorer.common;

import org.jetbrains.annotations.NotNull;

public class CompiledText {
    @NotNull
    private PreprocessedSource preprocessedSource;

    @NotNull
    private String compilerId;

    @NotNull
    private String compiledText;

    public CompiledText(@NotNull PreprocessedSource preprocessedSource_, @NotNull String compilerId_, @NotNull String compiledText_) {
        preprocessedSource = preprocessedSource_;
        compiledText = compiledText_;
        compilerId = compilerId_;
    }

    @NotNull
    public PreprocessedSource getPreprocessedSource() {
        return preprocessedSource;
    }

    @NotNull
    public String getCompilerId() {
        return compilerId;
    }

    @NotNull
    public String getCompiledText() {
        return compiledText;
    }
}
