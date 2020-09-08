package com.compilerexplorer.datamodel.state;

import com.google.gson.annotations.SerializedName;
import com.intellij.util.xmlb.annotations.Property;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.PROPERTY)
public class RemoteCompilerInfo {
    @Nullable
    @Property
    @SerializedName("id")
    private String id = "";
    @Nullable
    @Property
    @SerializedName("name")
    private String name = "";
    @Nullable
    @Property
    @SerializedName("lang")
    private String language = "";
    @Nullable
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
        return getStringOrEmpty(id);
    }

    private void setId(@Nullable String id_) {
        id = id_;
    }

    @NotNull
    public String getName() {
        return getStringOrEmpty(name);
    }

    private void setName(@Nullable String name_) {
        name = name_;
    }

    @NotNull
    public String getLanguage() {
        return getStringOrEmpty(language);
    }

    private void setLanguage(@Nullable String language_) {
        language = language_;
    }

    @NotNull
    public String getVersion() {
        return getStringOrEmpty(version);
    }

    private void setVersion(@Nullable String version_) {
        version = version_;
    }


    @NotNull
    private static String getStringOrEmpty(@Nullable String s) {
        return s != null ? s : "";
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
