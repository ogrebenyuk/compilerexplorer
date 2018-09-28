package com.compilerexplorer.datamodel.state;

import com.google.gson.annotations.SerializedName;
import com.intellij.util.xmlb.annotations.Property;
import org.jetbrains.annotations.NotNull;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.PROPERTY)
public class RemoteCompilerInfo {
    @NotNull
    @Property
    @SerializedName("id")
    private String id = "";
    @NotNull
    @Property
    @SerializedName("name")
    private String name = "";
    @NotNull
    @Property
    @SerializedName("lang")
    private String language = "";
    @NotNull
    @Property
    @SerializedName("version")
    private String version = "";

    RemoteCompilerInfo() {
        // empty
    }

    RemoteCompilerInfo(@NotNull RemoteCompilerInfo other) {
        copyFrom(other);
    }

    @NotNull
    public String getId() {
        return id;
    }

    private void setId(@NotNull String id_) {
        id = id_;
    }

    @NotNull
    public String getName() {
        return name;
    }

    private void setName(@NotNull String name_) {
        name = name_;
    }

    @NotNull
    public String getLanguage() {
        return language;
    }

    private void setLanguage(@NotNull String language_) {
        language = language_;
    }

    @NotNull
    public String getVersion() {
        return version;
    }

    private void setVersion(@NotNull String version_) {
        version = version_;
    }

    private void copyFrom(@NotNull RemoteCompilerInfo other) {
        setId(other.getId());
        setName(other.getName());
        setLanguage(other.getLanguage());
        setVersion(other.getVersion());
    }

    @Override
    public int hashCode() {
        return getId().hashCode()
                + getName().hashCode()
                + getLanguage().hashCode()
                + getVersion().hashCode()
                ;
    }

    @Override
    public boolean equals(@NotNull Object obj) {
        if (!(obj instanceof RemoteCompilerInfo)) {
            return false;
        }
        RemoteCompilerInfo other = (RemoteCompilerInfo) obj;
        return getId().equals(other.getId())
                && getName().equals(other.getName())
                && getLanguage().equals(other.getLanguage())
                && getVersion().equals(other.getVersion())
                ;
    }
}
