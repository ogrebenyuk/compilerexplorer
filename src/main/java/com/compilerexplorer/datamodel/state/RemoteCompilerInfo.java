package com.compilerexplorer.datamodel.state;

import com.google.gson.annotations.SerializedName;
import com.intellij.util.xmlb.annotations.Property;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.PROPERTY)
public class RemoteCompilerInfo {
    @NonNls
    @NotNull
    private static final String ID_FIELD = "id";
    @NonNls
    @NotNull
    private static final String NAME_FIELD = "name";
    @NonNls
    @NotNull
    private static final String LANG_FIELD = "lang";
    @NonNls
    @NotNull
    private static final String SEMVER_FIELD = "semver";
    @NonNls
    @NotNull
    public static final String RAW_DATA_FIELD = "rawData";

    @Nullable
    @Property
    @SerializedName(ID_FIELD)
    private String id = "";
    @Nullable
    @Property
    @SerializedName(NAME_FIELD)
    private String name = "";
    @Nullable
    @Property
    @SerializedName(LANG_FIELD)
    private String language = "";
    @Nullable
    @Property
    @SerializedName(SEMVER_FIELD)
    private String version = "";

    @NotNull
    private String rawData = "";

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
    public String getRawData() {
        return rawData;
    }

    public void setRawData(@NotNull String rawData_) {
        rawData = rawData_;
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
        setRawData(other.getRawData());
    }

    @Override
    public int hashCode() {
        return getId().hashCode()
                + getName().hashCode()
                + getLanguage().hashCode()
                + getVersion().hashCode()
                + getRawData().hashCode()
                ;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof RemoteCompilerInfo other)) {
            return false;
        }
        return getId().equals(other.getId())
                && getName().equals(other.getName())
                && getLanguage().equals(other.getLanguage())
                && getVersion().equals(other.getVersion())
                && getRawData().equals(other.getRawData())
                ;
    }
}
