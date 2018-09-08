package com.compilerexplorer.common.datamodel;

import com.compilerexplorer.common.datamodel.ProjectSettings;
import org.jetbrains.annotations.NotNull;

public interface ProjectSettingsConsumer {
    void setProjectSetting(@NotNull ProjectSettings projectSettings);
}
