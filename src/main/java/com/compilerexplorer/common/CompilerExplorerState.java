package com.compilerexplorer.common;

import com.intellij.util.xmlb.annotations.Property;
import org.jetbrains.annotations.NotNull;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.util.HashMap;
import java.util.Map;

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
    private Map<String, String> compilers = new HashMap<>();

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
    public Map<String, String> getCompilers() {
        return compilers;
    }

    public void setCompilers(@NotNull Map<String, String> compilers_) {
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
}
