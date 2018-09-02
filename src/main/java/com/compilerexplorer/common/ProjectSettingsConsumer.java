package com.compilerexplorer.common;

import org.jetbrains.annotations.NotNull;

public interface ProjectSettingsConsumer {
    void setProjectSetting(@NotNull ProjectSettings projectSettings);
}
