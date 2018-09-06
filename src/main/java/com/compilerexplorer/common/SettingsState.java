package com.compilerexplorer.common;

import com.intellij.util.xmlb.annotations.Property;
import org.jetbrains.annotations.NotNull;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@XmlAccessorType(XmlAccessType.PROPERTY)
public class SettingsState {
    @NotNull
    @Property
    private String url = "http://localhost:10240";
    @Property
    private boolean connected = false;
    @NotNull
    @Property
    private String lastConnectionStatus = "";
    @NotNull
    @Property
    private List<RemoteCompilerInfo> remoteCompilers = new ArrayList<>();
    @NotNull
    @Property
    private Map<String, String> remoteCompilerDefines = new HashMap<>();
    @NotNull
    @Property
    private Map<String, LocalCompilerSettings> localCompilerSettings = new HashMap<>();
    @NotNull
    @Property
    private Filters filters = new Filters();

    @NotNull
    public String getUrl() {
        return url;
    }

    public void setUrl(@NotNull String url_) {
        url = url_;
    }

    public boolean getConnected() {
        return connected;
    }

    public void setConnected(boolean connected_) {
        connected = connected_;
    }

    @NotNull
    public String getLastConnectionStatus() {
        return lastConnectionStatus;
    }

    public void setLastConnectionStatus(@NotNull String lastConnectionStatus_) {
        lastConnectionStatus = lastConnectionStatus_;
    }

    @NotNull
    public List<RemoteCompilerInfo> getRemoteCompilers() {
        return remoteCompilers;
    }

    public void setRemoteCompilers(@NotNull List<RemoteCompilerInfo> remoteCompilers_) {
        remoteCompilers = remoteCompilers_;
    }

    @NotNull
    public Map<String, String> getRemoteCompilerDefines() {
        return remoteCompilerDefines;
    }

    public void setRemoteCompilerDefines(@NotNull Map<String, String> remoteCompilerDefines_) {
        remoteCompilerDefines = remoteCompilerDefines_;
    }

    @NotNull
    public Map<String, LocalCompilerSettings> getLocalCompilerSettings() {
        return localCompilerSettings;
    }

    public void setLocalCompilerSettings(@NotNull Map<String, LocalCompilerSettings> localCompilers_) {
        localCompilerSettings = localCompilers_;
    }

    @NotNull
    public Filters getFilters() {
        return filters;
    }

    public void setFilters(@NotNull Filters filters_) {
        filters = filters_;
    }

    public boolean isConnectionCleared() {
        return !getConnected() && getLastConnectionStatus().isEmpty();
    }

    public void clearConnection() {
        connected = false;
        lastConnectionStatus = "";
        remoteCompilers = new ArrayList<>();
        remoteCompilerDefines = new HashMap<>();
    }

    public void clearLocalCompilers() {
        setLocalCompilerSettings(new HashMap<>());
    }

    public void copyFrom(@NotNull SettingsState other) {
        url = other.url;
        connected = other.connected;
        lastConnectionStatus = other.lastConnectionStatus;
        remoteCompilers = new ArrayList<>();
        for (RemoteCompilerInfo otherInfo : other.remoteCompilers) {
            RemoteCompilerInfo info = new RemoteCompilerInfo();
            info.copyFrom(otherInfo);
            remoteCompilers.add(info);
        }
        remoteCompilerDefines = new HashMap<>();
        for (Map.Entry<String, String> otherEntry : remoteCompilerDefines.entrySet()) {
            remoteCompilerDefines.put(otherEntry.getKey(), otherEntry.getValue());
        }
        localCompilerSettings = new HashMap<>();
        for (Map.Entry<String, LocalCompilerSettings> otherEntry : localCompilerSettings.entrySet()) {
            LocalCompilerSettings settings = new LocalCompilerSettings();
            settings.copyFrom(otherEntry.getValue());
            localCompilerSettings.put(otherEntry.getKey(), settings);
        }
        filters = new Filters();
        filters.copyFrom(other.filters);
    }

    @Override
    public int hashCode() {
        return url.hashCode()
             + (connected ? 1 : 0)
             + lastConnectionStatus.hashCode()
             + remoteCompilers.hashCode()
             + remoteCompilerDefines.hashCode()
             + localCompilerSettings.hashCode()
             + filters.hashCode()
        ;
    }

    @Override
    public boolean equals(@NotNull Object obj) {
        if (!(obj instanceof SettingsState)) {
            return false;
        }
        SettingsState other = (SettingsState)obj;
        return url.equals(other.url)
            && connected == other.connected
            && lastConnectionStatus.equals(other.lastConnectionStatus)
            && remoteCompilers.equals(other.remoteCompilers)
            && remoteCompilerDefines.equals(other.remoteCompilerDefines)
            && localCompilerSettings.equals(other.localCompilerSettings)
            && filters.equals(other.filters)
        ;
    }

    @XmlAccessorType(XmlAccessType.PROPERTY)
    public static class LocalCompilerSettings {
        @NotNull
        @Property
        private String name = "";
        @NotNull
        @Property
        private String version = "";
        @NotNull
        @Property
        private String target = "";

        @NotNull
        public String getName() {
            return name;
        }

        public void setName(@NotNull String name_) {
            name = name_;
        }

        @NotNull
        public String getVersion() {
            return version;
        }

        public void setVersion(@NotNull String version_) {
            version = version_;
        }

        @NotNull
        public String getTarget() {
            return target;
        }

        public void setTarget(@NotNull String target_) {
            target = target_;
        }

        public void copyFrom(@NotNull LocalCompilerSettings other) {
            name = other.name;
            version = other.version;
            target = other.target;
        }

        @Override
        public int hashCode() {
            return name.hashCode()
                 + version.hashCode()
                 + target.hashCode()
            ;
        }

        @Override
        public boolean equals(@NotNull Object obj) {
            if (!(obj instanceof LocalCompilerSettings)) {
                return false;
            }
            LocalCompilerSettings other = (LocalCompilerSettings)obj;
            return name.equals(other.name)
                && version.equals(other.version)
                && target.equals(other.target)
            ;
        }
    }

    @XmlAccessorType(XmlAccessType.PROPERTY)
    public static class RemoteCompilerInfo {
        @NotNull
        @Property
        private String id = "";

        @NotNull
        @Property
        private String name = "";

        @NotNull
        @Property
        private String language = "";

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
            id = other.id;
            name = other.name;
            language = other.language;
        }

        @Override
        public int hashCode() {
            return id.hashCode()
                    + name.hashCode()
                    + language.hashCode()
                    ;
        }

        @Override
        public boolean equals(@NotNull Object obj) {
            if (!(obj instanceof RemoteCompilerInfo)) {
                return false;
            }
            RemoteCompilerInfo other = (RemoteCompilerInfo) obj;
            return id.equals(other.id)
                    && name.equals(other.name)
                    && language.equals(other.language)
                    ;
        }
    }

    @XmlAccessorType(XmlAccessType.PROPERTY)
    public static class Filters {
        @Property
        private boolean intel = true;
        @Property
        private boolean commentOnly = true;
        @Property
        private boolean directives = true;
        @Property
        private boolean labels = true;
        @Property
        private boolean optOutput = false;

        public boolean getIntel() {
            return intel;
        }

        public void setIntel(boolean intel_) {
            intel = intel_;
        }

        public boolean getCommentOnly() {
            return commentOnly;
        }

        public void setCommentOnly(boolean commentOnly_) {
            commentOnly = commentOnly_;
        }

        public boolean getDirectives() {
            return directives;
        }

        public void setDirectives(boolean directives_) {
            directives = directives_;
        }

        public boolean getLabels() {
            return labels;
        }

        public void setLabels(boolean labels_) {
            labels = labels_;
        }

        public boolean getOptOutput() {
            return optOutput;
        }

        public void setOptOutput(boolean optOutput_) {
            optOutput = optOutput_;
        }

        public void copyFrom(@NotNull Filters other) {
            intel = other.intel;
            commentOnly = other.commentOnly;
            directives = other.directives;
            labels = other.labels;
            optOutput = other.optOutput;
        }

        @Override
        public int hashCode() {
            return 0;
        }

        @Override
        public boolean equals(@NotNull Object obj) {
            if (!(obj instanceof Filters)) {
                return false;
            }
            Filters other = (Filters)obj;
            return intel == other.intel
                && commentOnly == other.commentOnly
                && directives == other.directives
                && labels == other.labels
                && optOutput == other.optOutput
            ;
        }
    }

}
