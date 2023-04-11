package com.compilerexplorer.datamodel.state;

import com.intellij.util.xmlb.annotations.Property;
import org.jetbrains.annotations.NotNull;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.PROPERTY)
public class LocalCompilerPath {
    @NotNull
    @Property
    private String path = "";

    public LocalCompilerPath() {
        // empty
    }

    public LocalCompilerPath(@NotNull LocalCompilerPath other) {
        copyFrom(other);
    }

    public LocalCompilerPath(@NotNull String path_) {
        setPath(path_);
    }

    @NotNull
    private String getPath() {
        return path;
    }

    private void setPath(@NotNull String path_) {
        path = path_;
    }

    private void copyFrom(@NotNull LocalCompilerPath other) {
        setPath(other.getPath());
    }

    @Override
    public int hashCode() {
        return getPath().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof LocalCompilerPath other)) {
            return false;
        }
        return getPath().equals(other.getPath());
    }

    @Override
    @NotNull
    public String toString() {
        return getPath();
    }
}
