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
    private static final String COMPILER_TYPE_FIELD = "compilerType";
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
    @Nullable
    @Property
    @SerializedName(COMPILER_TYPE_FIELD)
    private String compilerType = "";

    @NotNull
    private String rawData = "";

    public RemoteCompilerInfo() {
        // empty
    }

    public RemoteCompilerInfo(@NotNull RemoteCompilerInfo other) {
        copyFrom(other);
    }

    @NotNull
    public String getId() {
        return getStringOrEmpty(id);
    }

    public void setId(@Nullable String id_) {
        id = id_;
    }

    @NotNull
    public String getName() {
        return getStringOrEmpty(name);
    }

    public void setName(@Nullable String name_) {
        name = name_;
    }

    @NotNull
    public String getLanguage() {
        return getStringOrEmpty(language);
    }

    public void setLanguage(@Nullable String language_) {
        language = language_;
    }

    @NotNull
    public String getVersion() {
        return getStringOrEmpty(version);
    }

    public void setVersion(@Nullable String version_) {
        version = version_;
    }

    @NotNull
    public String getCompilerType() {
        return getStringOrEmpty(compilerType);
    }

    public void setCompilerType(@Nullable String compilerType_) {
        compilerType = compilerType_;
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
        setCompilerType(other.getCompilerType());
        setRawData(other.getRawData());
    }

    @Override
    public int hashCode() {
        return getId().hashCode()
                + getName().hashCode()
                + getLanguage().hashCode()
                + getVersion().hashCode()
                + getCompilerType().hashCode()
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
                && getCompilerType().equals(other.getCompilerType())
                && getRawData().equals(other.getRawData())
                ;
    }
}
