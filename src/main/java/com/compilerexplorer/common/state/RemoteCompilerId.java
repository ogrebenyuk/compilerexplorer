package com.compilerexplorer.common.state;

import com.intellij.util.xmlb.annotations.Property;
import org.jetbrains.annotations.NotNull;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.PROPERTY)
public class RemoteCompilerId {
    @NotNull
    @Property
    private String id = "";

    public RemoteCompilerId() {
        // empty
    }

    public RemoteCompilerId(@NotNull RemoteCompilerId other) {
        copyFrom(other);
    }

    public RemoteCompilerId(@NotNull String id_) {
        setId(id_);
    }

    @NotNull
    public String getId() {
        return id;
    }

    public void setId(@NotNull String id_) {
        id = id_;
    }

    public void copyFrom(@NotNull RemoteCompilerId other) {
        setId(other.getId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode();
    }

    @Override
    public boolean equals(@NotNull Object obj) {
        if (!(obj instanceof RemoteCompilerId)) {
            return false;
        }
        RemoteCompilerId other = (RemoteCompilerId)obj;
        return getId().equals(other.getId());
    }
}
