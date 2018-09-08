package com.compilerexplorer.common.datamodel.state;

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

    public RemoteCompilerInfo() {
        // empty
    }

    public RemoteCompilerInfo(@NotNull RemoteCompilerInfo other) {
        copyFrom(other);
    }

    public RemoteCompilerInfo(@NotNull String id_, @NotNull String name_, @NotNull String language_) {
        setId(id_);
        setName(name_);
        setLanguage(language_);
    }

    @NotNull
    public String getId() {
        return id;
    }

    public void setId(@NotNull String id_) {
        id = id_;
    }

    @NotNull
    public String getName() {
        return name;
    }

    public void setName(@NotNull String name_) {
        name = name_;
    }

    @NotNull
    public String getLanguage() {
        return language;
    }

    public void setLanguage(@NotNull String language_) {
        language = language_;
    }

    public void copyFrom(@NotNull RemoteCompilerInfo other) {
        setId(other.getId());
        setName(other.getName());
        setLanguage(other.getLanguage());
    }

    @Override
    public int hashCode() {
        return getId().hashCode()
                + getName().hashCode()
                + getLanguage().hashCode()
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
                ;
    }
}
