package com.compilerexplorer.common.datamodel;

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
}
