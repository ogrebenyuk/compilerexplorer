package com.compilerexplorer.common;

import com.intellij.util.xmlb.annotations.Property;
import org.jetbrains.annotations.NotNull;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@XmlAccessorType(XmlAccessType.PROPERTY)
public class CompilerExplorerState {
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
    private List<CompilerInfo> compilers = new ArrayList<>();

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
    public List<CompilerInfo> getCompilers() {
        return compilers;
    }

    public void setCompilers(@NotNull List<CompilerInfo> compilers_) {
        compilers = compilers_;
    }

    public boolean isConnectionCleared() {
        return !getConnected() && getLastConnectionStatus().isEmpty();
    }

    public void clearConnection() {
        setConnected(false);
        setLastConnectionStatus("");
    }

    public void copyFrom(@NotNull CompilerExplorerState other) {
        url = other.url;
        connected = other.connected;
        lastConnectionStatus = other.lastConnectionStatus;
        compilers = other.compilers;
    }

    @Override
    public int hashCode() {
        return url.hashCode()
             + (connected ? 1 : 0)
             + lastConnectionStatus.hashCode()
             + compilers.hashCode()
        ;
    }

    @Override
    public boolean equals(@NotNull Object obj) {
        if (!(obj instanceof CompilerExplorerState)) {
            return false;
        }
        CompilerExplorerState other = (CompilerExplorerState)obj;
        return url.equals(other.url)
            && connected == other.connected
            && lastConnectionStatus.equals(other.lastConnectionStatus)
            && compilers.equals(other.compilers)
        ;
    }

    @XmlAccessorType(XmlAccessType.PROPERTY)
    public static class CompilerInfo {
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
            language =language_;
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
            if (!(obj instanceof CompilerInfo)) {
                return false;
            }
            CompilerInfo other = (CompilerInfo)obj;
            return id.equals(other.id)
                && name.equals(other.name)
                && language.equals(other.language)
            ;
        }
    }
}
