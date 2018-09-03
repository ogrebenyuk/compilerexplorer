package com.compilerexplorer.common;

import com.intellij.util.xmlb.annotations.Property;
import org.jetbrains.annotations.NotNull;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.PROPERTY)
public class CompilerExplorerState {
    @NotNull
    @Property
    private String url = "https://localhost:12345";

    @Property
    private boolean connected = false;

    @NotNull
    @Property
    private String lastConnectionStatus = "";

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

    public void copyFrom(@NotNull CompilerExplorerState other) {
        url = other.url;
        connected = other.connected;
        lastConnectionStatus = other.lastConnectionStatus;
    }

    @Override
    public int hashCode() {
        return url.hashCode()
             + (connected ? 1 : 0)
             + lastConnectionStatus.hashCode();
    }

    @Override
    public boolean equals(@NotNull Object obj) {
        if (!(obj instanceof CompilerExplorerState)) {
            return false;
        }
        CompilerExplorerState other = (CompilerExplorerState)obj;
        return url.equals(other.url)
            && connected == other.connected
            && lastConnectionStatus.equals(other.lastConnectionStatus);
    }
}
