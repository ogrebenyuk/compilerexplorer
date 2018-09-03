package com.compilerexplorer.common;

import org.jetbrains.annotations.NotNull;

public interface PreprocessedSourceConsumer {
    void setPreprocessedSource(@NotNull PreprocessedSource preprocessedSource);
    void clearPreprocessedSource(@NotNull String reason);
}
