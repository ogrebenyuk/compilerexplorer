package com.compilerexplorer.datamodel.state;

import com.intellij.util.xmlb.annotations.Property;
import org.jetbrains.annotations.NotNull;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.PROPERTY)
public class LocalCompilerSettings {
    @NotNull
    @Property
    private String name = "";
    @NotNull
    @Property
    private String version = "";
    @NotNull
    @Property
    private String target = "";

    public LocalCompilerSettings() {
        // empty
    }

    public LocalCompilerSettings(@NotNull LocalCompilerSettings other) {
        copyFrom(other);
    }

    public LocalCompilerSettings(@NotNull String name_, @NotNull String version_, @NotNull String target_) {
        setName(name_);
        setVersion(version_);
        setTarget(target_);
    }

    @NotNull
    public String getName() {
        return name;
    }

    private void setName(@NotNull String name_) {
        name = name_;
    }

    @NotNull
    public String getVersion() {
        return version;
    }

    private void setVersion(@NotNull String version_) {
        version = version_;
    }

    @NotNull
    public String getTarget() {
        return target;
    }

    private void setTarget(@NotNull String target_) {
        target = target_;
    }

    private void copyFrom(@NotNull LocalCompilerSettings other) {
        setName(other.getName());
        setVersion(other.getVersion());
        setTarget(other.getTarget());
    }

    @Override
    public int hashCode() {
        return getName().hashCode()
             + getVersion().hashCode()
             + getTarget().hashCode()
        ;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof LocalCompilerSettings other)) {
            return false;
        }
        return getName().equals(other.getName())
            && getVersion().equals(other.getVersion())
            && getTarget().equals(other.getTarget())
        ;
    }
}
