package com.compilerexplorer.common;

import org.jetbrains.annotations.NotNull;

public interface PreprocessableSourceConsumer {
    void setPreprocessableSource(@NotNull PreprocessableSource preprocessableSource);
    void clearPreprocessableSource(@NotNull String reason);
}
