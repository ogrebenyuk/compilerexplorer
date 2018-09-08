package com.compilerexplorer.common.datamodel;

import com.compilerexplorer.common.datamodel.PreprocessableSource;
import org.jetbrains.annotations.NotNull;

public interface PreprocessableSourceConsumer {
    void setPreprocessableSource(@NotNull PreprocessableSource preprocessableSource);
    void clearPreprocessableSource(@NotNull String reason);
}
