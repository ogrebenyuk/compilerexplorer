package com.compilerexplorer.datamodel;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Vector;

public class ProjectSettings implements Visitable {
    @NotNull
    private final List<SourceSettings> settings;

    public ProjectSettings(@NotNull Vector<SourceSettings> settings_) {
        settings = settings_;
    }

    @NotNull
    public List<SourceSettings> getSettings() {
        return settings;
    }

    @Override
    public void accept(@NotNull Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public int hashCode() {
        return getSettings().hashCode()
                ;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ProjectSettings other)) {
            return false;
        }
        return getSettings().equals(other.getSettings())
                ;
    }
}
