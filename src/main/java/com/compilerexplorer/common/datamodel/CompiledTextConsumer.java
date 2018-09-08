package com.compilerexplorer.common.datamodel;

import org.jetbrains.annotations.NotNull;

public interface CompiledTextConsumer {
    void setCompiledText(@NotNull CompiledText compiledText);
    void clearCompiledText(@NotNull String reason);
}
