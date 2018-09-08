package com.compilerexplorer.common.state;

import com.intellij.util.xmlb.annotations.Property;
import org.jetbrains.annotations.NotNull;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.PROPERTY)
public class Defines {
    @NotNull
    @Property
    private String defines = "";

    public Defines() {
        // empty
    }

    public Defines(@NotNull Defines other) {
        copyFrom(other);
    }

    public Defines(@NotNull String defines_) {
        setDefines(defines_);
    }

    @NotNull
    public String getDefines() {
        return defines;
    }

    public void setDefines(@NotNull String defines_) {
        defines = defines_;
    }

    public void copyFrom(@NotNull Defines other) {
        setDefines(other.getDefines());
    }

    @Override
    public int hashCode() {
        return getDefines().hashCode();
    }

    @Override
    public boolean equals(@NotNull Object obj) {
        if (!(obj instanceof Defines)) {
            return false;
        }
        Defines other = (Defines)obj;
        return getDefines().equals(other.getDefines());
    }
}
