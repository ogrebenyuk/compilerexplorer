package com.compilerexplorer.common.datamodel.state;

import com.intellij.util.xmlb.annotations.Property;
import org.jetbrains.annotations.NotNull;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.util.*;

@XmlAccessorType(XmlAccessType.PROPERTY)
public class SettingsState {
    @NotNull
    public static final String DEFAULT_URL = "http://localhost:10240";
    public static final boolean DEFAULT_CONNECTED = false;
    public static final boolean DEFAULT_ALLOW_MINOR_VERSION_MISMATCH = true;
    public static final boolean DEFAULT_PREPROCESS_LOCALLY = true;
    public static final boolean DEFAULT_USE_REMOTE_DEFINES = false;
    @NotNull
    public static final String DEFAULT_ADDITIONAL_SWITCHES = "-fverbose-asm";

    @NotNull
    private static final SettingsState EMPTY = new SettingsState();

    @NotNull
    @Property
    private String url = DEFAULT_URL;
    @Property
    private boolean connected = DEFAULT_CONNECTED;
    @NotNull
    @Property
    private String lastConnectionStatus = "";
    @NotNull
    @Property
    private List<RemoteCompilerInfo> remoteCompilers = new ArrayList<>();
    @NotNull
    @Property
    private Map<RemoteCompilerId, Defines> remoteCompilerDefines = new HashMap<>();
    @NotNull
    @Property
    private Map<LocalCompilerPath, LocalCompilerSettings> localCompilerSettings = new HashMap<>();
    @NotNull
    @Property
    private Filters filters = new Filters();
    @Property
    private boolean allowMinorVersionMismatch = DEFAULT_ALLOW_MINOR_VERSION_MISMATCH;
    @NotNull
    @Property
    private Map<LocalCompilerPath, CompilerMatches> compilerMatches = new HashMap<>();
    @Property
    private boolean preprocessLocally = DEFAULT_PREPROCESS_LOCALLY;
    @Property
    private boolean useRemoteDefines = DEFAULT_USE_REMOTE_DEFINES;
    @NotNull
    @Property
    private String additionalSwitches = DEFAULT_ADDITIONAL_SWITCHES;

    public SettingsState() {
        // empty
    }

    public SettingsState(@NotNull SettingsState other) {
        copyFrom(other);
    }

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
        remoteCompilers = new ArrayList<>();
        remoteCompilers_.forEach(otherInfo -> remoteCompilers.add(new RemoteCompilerInfo(otherInfo)));
    }

    @NotNull
    public Map<RemoteCompilerId, Defines> getRemoteCompilerDefines() {
        return remoteCompilerDefines;
    }

    public void setRemoteCompilerDefines(@NotNull Map<RemoteCompilerId, Defines> remoteCompilerDefines_) {
        remoteCompilerDefines = new HashMap<>();
        remoteCompilerDefines_.forEach((key, value) -> remoteCompilerDefines.put(new RemoteCompilerId(key), new Defines(value)));
    }

    @NotNull
    public Map<LocalCompilerPath, LocalCompilerSettings> getLocalCompilerSettings() {
        return localCompilerSettings;
    }

    public void setLocalCompilerSettings(@NotNull Map<LocalCompilerPath, LocalCompilerSettings> localCompilers_) {
        localCompilerSettings = new HashMap<>();
        localCompilers_.forEach((key, value) -> localCompilerSettings.put(new LocalCompilerPath(key), new LocalCompilerSettings(value)));
    }

    @NotNull
    public Filters getFilters() {
        return filters;
    }

    public void setFilters(@NotNull Filters filters_) {
        filters = new Filters(filters_);
    }

    public boolean getAllowMinorVersionMismatch() {
        return allowMinorVersionMismatch;
    }

    public void setAllowMinorVersionMismatch(boolean allowMinorVersionMismatch_) {
        allowMinorVersionMismatch = allowMinorVersionMismatch_;
    }

    @NotNull
    public Map<LocalCompilerPath, CompilerMatches> getCompilerMatches() {
        return compilerMatches;
    }

    public void setCompilerMatches(@NotNull Map<LocalCompilerPath, CompilerMatches> compilerMatches_) {
        compilerMatches = new HashMap<>();
        compilerMatches_.forEach((key, value) -> compilerMatches.put(new LocalCompilerPath(key), new CompilerMatches(value)));
    }

    public boolean getPreprocessLocally() {
        return preprocessLocally;
    }

    public void setPreprocessLocally(boolean preprocessLocally_) {
        preprocessLocally = preprocessLocally_;
    }

    public boolean getUseRemoteDefines() {
        return useRemoteDefines;
    }

    public void setUseRemoteDefines(boolean useRemoteDefines_) {
        useRemoteDefines = useRemoteDefines_;
    }

    @NotNull
    public String getAdditionalSwitches() {
        return additionalSwitches;
    }

    public void setAdditionalSwitches(@NotNull String additionalSwitches_) {
        additionalSwitches = additionalSwitches_;
    }

    public boolean isConnectionCleared() {
        return !getConnected() && getLastConnectionStatus().isEmpty();
    }

    public void clearConnection() {
        setConnected(EMPTY.getConnected());
        setLastConnectionStatus(EMPTY.getLastConnectionStatus());
        setRemoteCompilers(EMPTY.getRemoteCompilers());
        setRemoteCompilerDefines(EMPTY.getRemoteCompilerDefines());
        setCompilerMatches(EMPTY.getCompilerMatches());
    }

    public void clearLocalCompilerSettings() {
        setLocalCompilerSettings(EMPTY.getLocalCompilerSettings());
    }

    public void clearCompilerMatches() {
        setCompilerMatches(EMPTY.getCompilerMatches());
    }

    public void copyFrom(@NotNull SettingsState other) {
        setUrl(other.getUrl());
        setConnected(other.getConnected());
        setLastConnectionStatus(other.getLastConnectionStatus());
        setRemoteCompilers(other.getRemoteCompilers());
        setRemoteCompilerDefines(other.getRemoteCompilerDefines());
        setLocalCompilerSettings(other.getLocalCompilerSettings());
        setFilters(other.getFilters());
        setAllowMinorVersionMismatch(other.getAllowMinorVersionMismatch());
        setCompilerMatches(other.getCompilerMatches());
        setPreprocessLocally(other.getPreprocessLocally());
        setUseRemoteDefines(other.getUseRemoteDefines());
        setAdditionalSwitches(other.getAdditionalSwitches());
    }

    @Override
    public int hashCode() {
        return getUrl().hashCode()
                + (getConnected() ? 1 : 0)
                + getLastConnectionStatus().hashCode()
                + getRemoteCompilers().hashCode()
                + getRemoteCompilerDefines().hashCode()
                + getLocalCompilerSettings().hashCode()
                + getFilters().hashCode()
                + (getAllowMinorVersionMismatch() ? 1 : 0)
                + getCompilerMatches().hashCode()
                + (getPreprocessLocally() ? 1 : 0)
                + (getUseRemoteDefines() ? 1 : 0)
                + getAdditionalSwitches().hashCode()
                ;
    }

    @Override
    public boolean equals(@NotNull Object obj) {
        if (!(obj instanceof SettingsState)) {
            return false;
        }
        SettingsState other = (SettingsState)obj;
        return getUrl().equals(other.getUrl())
                && getConnected() == other.getConnected()
                && getLastConnectionStatus().equals(other.getLastConnectionStatus())
                && getRemoteCompilers().equals(other.getRemoteCompilers())
                && getRemoteCompilerDefines().equals(other.getRemoteCompilerDefines())
                && getLocalCompilerSettings().equals(other.getLocalCompilerSettings())
                && getFilters().equals(other.getFilters())
                && getAllowMinorVersionMismatch() == other.getAllowMinorVersionMismatch()
                && getCompilerMatches().equals(other.getCompilerMatches())
                && getPreprocessLocally() == other.getPreprocessLocally()
                && getUseRemoteDefines() == other.getUseRemoteDefines()
                && getAdditionalSwitches().equals(other.getAdditionalSwitches())
                ;
    }
}
