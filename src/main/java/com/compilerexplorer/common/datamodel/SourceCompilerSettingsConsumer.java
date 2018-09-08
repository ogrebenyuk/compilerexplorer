package com.compilerexplorer.common.datamodel;

import com.compilerexplorer.common.datamodel.SourceCompilerSettings;
import org.jetbrains.annotations.NotNull;

public interface SourceCompilerSettingsConsumer {
    void setSourceCompilerSetting(@NotNull SourceCompilerSettings sourceCompilerSettings);
    void clearSourceCompilerSetting(@NotNull String reason);
}
