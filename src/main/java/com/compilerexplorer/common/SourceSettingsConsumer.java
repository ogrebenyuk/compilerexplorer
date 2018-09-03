package com.compilerexplorer.common;

import org.jetbrains.annotations.NotNull;

public interface SourceSettingsConsumer {
    void setSourceSetting(@NotNull SourceSettings sourceSettings);
    void clearSourceSetting(@NotNull String reason);
}
