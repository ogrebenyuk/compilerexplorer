package com.compilerexplorer.common.datamodel;

import com.compilerexplorer.common.datamodel.SourceSettings;
import org.jetbrains.annotations.NotNull;

public interface SourceSettingsConsumer {
    void setSourceSetting(@NotNull SourceSettings sourceSettings);
    void clearSourceSetting(@NotNull String reason);
}
