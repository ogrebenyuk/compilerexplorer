package com.compilerexplorer.datamodel.state;

import com.google.gson.annotations.SerializedName;
import com.intellij.util.xmlb.annotations.Property;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.util.ArrayList;
import java.util.List;

@XmlAccessorType(XmlAccessType.PROPERTY)
public class RemoteLibraryInfo {
    public static final class Version {
        @NonNls
        @NotNull
        private static final String ID_FIELD = "id";
        @NonNls
        @NotNull
        private static final String VERSION_FIELD = "version";

        @Nullable
        @Property
        @SerializedName(ID_FIELD)
        private String id = "";
        @Nullable
        @Property
        @SerializedName(VERSION_FIELD)
        private String version = "";

        public Version() {
            // empty
        }

        public Version(@NotNull Version other) {
            id = other.id;
            version = other.version;
        }

        @NotNull
        public String getId() {
            return getStringOrEmpty(id);
        }

        public void setId(@Nullable String id_) {
            id = id_;
        }

        @NotNull
        public String getVersion() {
            return getStringOrEmpty(version);
        }

        public void setVersion(@Nullable String version_) {
            version = version_;
        }

        @Override
        public int hashCode() {
            return getId().hashCode() + getVersion().hashCode();
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Version other)) {
                return false;
            }
            return getId().equals(other.getId()) && getVersion().equals(other.getVersion());
        }
    }

    @NonNls
    @NotNull
    private static final String ID_FIELD = "id";
    @NonNls
    @NotNull
    private static final String NAME_FIELD = "name";
    @NonNls
    @NotNull
    private static final String DESCRIPTION_FIELD = "description";
    @NonNls
    @NotNull
    private static final String VERSIONS_FIELD = "versions";
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
    @SerializedName(DESCRIPTION_FIELD)
    private String description = "";
    @Property
    @SerializedName(VERSIONS_FIELD)
    private List<Version> versions = new ArrayList<>();

    @NotNull
    private String rawData = "";

    public RemoteLibraryInfo() {
        // empty
    }

    public RemoteLibraryInfo(@NotNull RemoteLibraryInfo other) {
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
    public String getDescription() {
        return getStringOrEmpty(description);
    }

    public void setDescription(@Nullable String description_) {
        description = description_;
    }

    @NotNull
    public String getRawData() {
        return rawData;
    }

    public void setRawData(@NotNull String rawData_) {
        rawData = rawData_;
    }

    @NotNull
    public List<Version> getVersions() {
        return new ArrayList<>(versions);
    }

    public void setVersions(@NotNull List<Version> versions_) {
        versions = new ArrayList<>(versions_);
    }

    @NotNull
    private static String getStringOrEmpty(@Nullable String s) {
        return s != null ? s : "";
    }

    private void copyFrom(@NotNull RemoteLibraryInfo other) {
        setId(other.getId());
        setName(other.getName());
        setDescription(other.getDescription());
        setVersions(other.getVersions());
        setRawData(other.getRawData());
    }

    @Override
    public int hashCode() {
        return getId().hashCode()
                + getName().hashCode()
                + getDescription().hashCode()
                + getRawData().hashCode()
                ;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof RemoteLibraryInfo other)) {
            return false;
        }
        return getId().equals(other.getId())
                && getName().equals(other.getName())
                && getDescription().equals(other.getDescription())
                && getRawData().equals(other.getRawData())
                ;
    }
}
