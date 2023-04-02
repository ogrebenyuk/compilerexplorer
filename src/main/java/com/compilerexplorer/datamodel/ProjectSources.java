package com.compilerexplorer.datamodel;

import com.intellij.openapi.util.Key;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Vector;

public class ProjectSources implements Visitable {
    public static final Key<ProjectSources> KEY = Key.create(ProjectSources.class.getName());

    @NotNull
    private final List<SourceSettings> sources;

    public ProjectSources(@NotNull Vector<SourceSettings> sources_) {
        sources = sources_;
    }

    @NotNull
    public List<SourceSettings> getSources() {
        return sources;
    }

    @Override
    public void accept(@NotNull Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public int hashCode() {
        return sources.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof ProjectSources other)) {
            return false;
        }
        return sources.equals(other.sources);
    }
}
