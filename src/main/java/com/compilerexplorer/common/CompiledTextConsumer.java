package com.compilerexplorer.common;

import org.jetbrains.annotations.NotNull;

public interface CompiledTextConsumer {
    void setCompiledText(@NotNull String mainText);
    void clearCompiledText(@NotNull String reason);
}
