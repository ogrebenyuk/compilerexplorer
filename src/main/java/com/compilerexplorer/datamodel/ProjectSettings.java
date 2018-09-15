package com.compilerexplorer.datamodel;

import org.jetbrains.annotations.NotNull;

import java.util.Vector;

public class ProjectSettings {
    @NotNull
    private final Vector<SourceSettings> settings;

    public ProjectSettings(@NotNull Vector<SourceSettings> settings_) {
        settings = settings_;
    }

    @NotNull
    public Vector<SourceSettings> getSettings() {
        return settings;
    }

    @Override
    public int hashCode() {
        return getSettings().hashCode()
                ;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ProjectSettings)) {
            return false;
        }
        ProjectSettings other = (ProjectSettings)obj;
        return getSettings().equals(other.getSettings())
                ;
    }
}
