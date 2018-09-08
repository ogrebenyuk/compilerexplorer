package com.compilerexplorer.common.datamodel;

import com.compilerexplorer.common.datamodel.PreprocessedSource;
import org.jetbrains.annotations.NotNull;

public interface PreprocessedSourceConsumer {
    void setPreprocessedSource(@NotNull PreprocessedSource preprocessedSource);
    void clearPreprocessedSource(@NotNull String reason);
}
