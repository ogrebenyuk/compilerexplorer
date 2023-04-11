package com.compilerexplorer.datamodel.state;

import com.google.gson.annotations.SerializedName;
import com.intellij.util.xmlb.annotations.Property;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.PROPERTY)
public class EnabledRemoteLibraryInfo {
    @NonNls
    @NotNull
    private static final String ID_FIELD = "id";
    @NonNls
    @NotNull
    private static final String VERSION_ID_FIELD = "version";

    @Nullable
    @Property
    @SerializedName(ID_FIELD)
    private String id = "";
    @Nullable
    @Property
    @SerializedName(VERSION_ID_FIELD)
    private String versionId = "";

    public EnabledRemoteLibraryInfo() {
        // empty
    }

    public EnabledRemoteLibraryInfo(@NotNull EnabledRemoteLibraryInfo other) {
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
    public String getVersionId() {
        return getStringOrEmpty(versionId);
    }

    public void setVersionId(@Nullable String versionId_) {
        versionId = versionId_;
    }

    @NotNull
    private static String getStringOrEmpty(@Nullable String s) {
        return s != null ? s : "";
    }

    private void copyFrom(@NotNull EnabledRemoteLibraryInfo other) {
        setId(other.getId());
        setVersionId(other.getVersionId());
    }

    @Override
    public int hashCode() {
        return getId().hashCode() + getVersionId().hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof EnabledRemoteLibraryInfo other)) {
            return false;
        }
        return getId().equals(other.getId()) && getVersionId().equals(other.getVersionId());
    }
}
