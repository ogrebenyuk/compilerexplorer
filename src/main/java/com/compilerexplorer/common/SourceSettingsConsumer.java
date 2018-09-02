package com.compilerexplorer.common;

import com.compilerexplorer.common.ProjectSettings;
import org.jetbrains.annotations.NotNull;

public interface SourceSettingsConsumer {
    void setSourceSetting(@NotNull SourceSettings sourceSettings);
    void clearSourceSetting();
}
