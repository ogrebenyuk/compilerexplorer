package com.compilerexplorer.datamodel;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class SelectedSourceSettings {
    @NotNull
    public final ProjectSettings projectSettings;

    @Nullable
    public SourceSettings selectedSourceSettings;

    public SelectedSourceSettings(@NotNull ProjectSettings projectSettings_) {
        projectSettings = projectSettings_;
    }

    public boolean isValid() {
        return selectedSourceSettings != null;
    }

    @Override
    public int hashCode() {
        return projectSettings.hashCode()
                + (selectedSourceSettings != null ? selectedSourceSettings.hashCode() : 0)
                ;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof SelectedSourceSettings other)) {
            return false;
        }
        return projectSettings.equals(other.projectSettings)
                && Objects.equals(selectedSourceSettings, other.selectedSourceSettings)
                ;
    }
}
