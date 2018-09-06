package com.compilerexplorer.common;

import org.jetbrains.annotations.NotNull;

public interface SourceCompilerSettingsConsumer {
    void setSourceCompilerSetting(@NotNull SourceCompilerSettings sourceCompilerSettings);
    void clearSourceCompilerSetting(@NotNull String reason);
}
