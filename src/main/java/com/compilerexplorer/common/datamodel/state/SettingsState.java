package com.compilerexplorer.common.datamodel.state;

import com.intellij.util.xmlb.annotations.Property;
import org.jetbrains.annotations.NotNull;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.util.*;

@XmlAccessorType(XmlAccessType.PROPERTY)
public class SettingsState {
    @NotNull
    private static final String DEFAULT_URL = "http://localhost:10240";
    private static final boolean DEFAULT_CONNECTED = false;
    private static final boolean DEFAULT_PREPROCESS_LOCALLY = true;
    @NotNull
    private static final String DEFAULT_ADDITIONAL_SWITCHES = "-fverbose-asm";

    @NotNull
    public static final SettingsState EMPTY = new SettingsState();

    @Property
    private boolean enabled = true;
    @NotNull
    @Property
    private String url = DEFAULT_URL;
    @Property
    private boolean connected = DEFAULT_CONNECTED;
    @NotNull
    @Property
    private List<RemoteCompilerInfo> remoteCompilers = new ArrayList<>();
    @NotNull
    @Property
    private Map<LocalCompilerPath, LocalCompilerSettings> localCompilerSettings = new HashMap<>();
    @NotNull
    @Property
    private Filters filters = new Filters();
    @NotNull
    @Property
    private Map<LocalCompilerPath, CompilerMatches> compilerMatches = new HashMap<>();
    @Property
    private boolean preprocessLocally = DEFAULT_PREPROCESS_LOCALLY;
    @NotNull
    @Property
    private String additionalSwitches = DEFAULT_ADDITIONAL_SWITCHES;
    @Property
    private boolean autoscrollFromSource = false;
    @Property
    private boolean autoscrollToSource = false;
    @Property
    private boolean autoupdateFromSource = true;

    public SettingsState() {
        // empty
    }

    public SettingsState(@NotNull SettingsState other) {
        copyFrom(other);
    }

    public boolean getEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled_) {
        enabled = enabled_;
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
    public List<RemoteCompilerInfo> getRemoteCompilers() {
        return remoteCompilers;
    }

    public void setRemoteCompilers(@NotNull List<RemoteCompilerInfo> remoteCompilers_) {
        remoteCompilers = new ArrayList<>();
        remoteCompilers_.forEach(otherInfo -> remoteCompilers.add(new RemoteCompilerInfo(otherInfo)));
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

    @NotNull
    public String getAdditionalSwitches() {
        return additionalSwitches;
    }

    public void setAdditionalSwitches(@NotNull String additionalSwitches_) {
        additionalSwitches = additionalSwitches_;
    }

    public boolean getAutoscrollFromSource() {
        return autoscrollFromSource;
    }

    public void setAutoscrollFromSource(boolean autoscrollFromSource_) {
        autoscrollFromSource = autoscrollFromSource_;
    }

    public boolean getAutoscrollToSource() {
        return autoscrollToSource;
    }

    public void setAutoscrollToSource(boolean autoscrollToSource_) {
        autoscrollToSource = autoscrollToSource_;
    }

    public boolean getAutoupdateFromSource() {
        return autoupdateFromSource;
    }

    public void setAutoupdateFromSource(boolean autoupdateFromSource_) {
        autoupdateFromSource = autoupdateFromSource_;
    }

    public void copyFrom(@NotNull SettingsState other) {
        setEnabled(other.getEnabled());
        setUrl(other.getUrl());
        setConnected(other.getConnected());
        setRemoteCompilers(other.getRemoteCompilers());
        setLocalCompilerSettings(other.getLocalCompilerSettings());
        setFilters(other.getFilters());
        setCompilerMatches(other.getCompilerMatches());
        setPreprocessLocally(other.getPreprocessLocally());
        setAdditionalSwitches(other.getAdditionalSwitches());
        setAutoscrollFromSource(other.getAutoscrollFromSource());
        setAutoscrollToSource(other.getAutoscrollToSource());
        setAutoupdateFromSource(other.getAutoupdateFromSource());
    }

    @Override
    public int hashCode() {
        return (getEnabled() ? 1 : 0)
                + getUrl().hashCode()
                + (getConnected() ? 1 : 0)
                + getRemoteCompilers().hashCode()
                + getLocalCompilerSettings().hashCode()
                + getFilters().hashCode()
                + getCompilerMatches().hashCode()
                + (getPreprocessLocally() ? 1 : 0)
                + getAdditionalSwitches().hashCode()
                + (getAutoscrollFromSource() ? 1 : 0)
                + (getAutoscrollToSource() ? 1 : 0)
                + (getAutoupdateFromSource() ? 1 : 0)
                ;
    }

    @Override
    public boolean equals(@NotNull Object obj) {
        if (!(obj instanceof SettingsState)) {
            return false;
        }
        SettingsState other = (SettingsState)obj;
        return getEnabled() == other.getEnabled()
                && getUrl().equals(other.getUrl())
                && getConnected() == other.getConnected()
                && getRemoteCompilers().equals(other.getRemoteCompilers())
                && getLocalCompilerSettings().equals(other.getLocalCompilerSettings())
                && getFilters().equals(other.getFilters())
                && getCompilerMatches().equals(other.getCompilerMatches())
                && getPreprocessLocally() == other.getPreprocessLocally()
                && getAdditionalSwitches().equals(other.getAdditionalSwitches())
                && getAutoscrollFromSource() == (other.getAutoscrollFromSource())
                && getAutoscrollToSource() == (other.getAutoscrollToSource())
                && getAutoupdateFromSource() == (other.getAutoupdateFromSource())
                ;
    }
}
