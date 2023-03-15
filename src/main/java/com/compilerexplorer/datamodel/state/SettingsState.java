package com.compilerexplorer.datamodel.state;

import com.compilerexplorer.common.Constants;
import com.intellij.ui.JBColor;
import com.intellij.util.xmlb.annotations.Property;
import com.intellij.util.xmlb.annotations.Transient;
import org.jetbrains.annotations.NotNull;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.awt.*;
import java.util.*;
import java.util.List;

@XmlAccessorType(XmlAccessType.PROPERTY)
public class SettingsState {
    @NotNull
    private static final String DEFAULT_URL = Constants.DEFAULT_URL;
    private static final boolean DEFAULT_CONNECTED = false;
    private static final boolean DEFAULT_PREPROCESS_LOCALLY = true;
    @NotNull
    private static final String DEFAULT_ADDITIONAL_SWITCHES = Constants.DEFAULT_ADDITIONAL_SWITCHES;
    @NotNull
    private static final String DEFAULT_IGNORE_SWITCHES = Constants.DEFAULT_IGNORE_SWITCHES;
    @NotNull
    private static final Color DEFAULT_HIGHLIGHT_COLOR = Constants.DEFAULT_HIGHLIGHT_COLOR;
    private static final long DEFAULT_DELAY_MILLIS = Constants.DEFAULT_DELAY_MILLIS;
    private static final int DEFAULT_COMPILER_TIMEOUT_MILLIS = Constants.DEFAULT_COMPILER_TIMEOUT_MILLIS;

    @NotNull
    public static final SettingsState EMPTY = new SettingsState();

    @Transient
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
    @NotNull
    @Property
    private String ignoreSwitches = DEFAULT_IGNORE_SWITCHES;
    @Property
    private boolean autoscrollFromSource = false;
    @Property
    private boolean autoscrollToSource = false;
    @Property
    private boolean autoupdateFromSource = true;
    @Property
    private boolean shortenTemplates = false;
    @Property
    private int highlightColorRGB = DEFAULT_HIGHLIGHT_COLOR.getRGB();
    @Property
    private long delayMillis = DEFAULT_DELAY_MILLIS;
    @Property
    private int compilerTimeoutMillis = DEFAULT_COMPILER_TIMEOUT_MILLIS;
    @Property
    private boolean initialNoticeShown = false;

    public SettingsState() {
        // empty
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

    private void setFilters(@NotNull Filters filters_) {
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

    @NotNull
    public String getIgnoreSwitches() {
        return ignoreSwitches;
    }

    public void setIgnoreSwitches(@NotNull String ignoreSwitches_) {
        ignoreSwitches = ignoreSwitches_;
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

    public boolean getShortenTemplates() {
        return shortenTemplates;
    }

    public void setShortenTemplates(boolean shortenTemplates_) {
        shortenTemplates = shortenTemplates_;
    }

    public int getHighlightColorRGB() {
        return highlightColorRGB;
    }

    public void setHighlightColorRGB(int highlightColorRGB_) {
        highlightColorRGB = highlightColorRGB_;
    }

    public long getDelayMillis() {
        return delayMillis;
    }

    public void setDelayMillis(long delayMillis_) {
        delayMillis = delayMillis_;
    }

    public int getCompilerTimeoutMillis() {
        return compilerTimeoutMillis;
    }

    public void setCompilerTimeoutMillis(int compilerTimeoutMillis_) {
        compilerTimeoutMillis = compilerTimeoutMillis_;
    }

    public boolean getInitialNoticeShown() {
        return initialNoticeShown;
    }

    public void setInitialNoticeShown(boolean initialNoticeShown_) {
        initialNoticeShown = initialNoticeShown_;
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
        setIgnoreSwitches(other.getIgnoreSwitches());
        setAutoscrollFromSource(other.getAutoscrollFromSource());
        setAutoscrollToSource(other.getAutoscrollToSource());
        setAutoupdateFromSource(other.getAutoupdateFromSource());
        setShortenTemplates(other.getShortenTemplates());
        setHighlightColorRGB(other.getHighlightColorRGB());
        setDelayMillis(other.getDelayMillis());
        setCompilerTimeoutMillis(other.getCompilerTimeoutMillis());
        setInitialNoticeShown(other.getInitialNoticeShown());
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
                + getIgnoreSwitches().hashCode()
                + (getAutoscrollFromSource() ? 1 : 0)
                + (getAutoscrollToSource() ? 1 : 0)
                + (getAutoupdateFromSource() ? 1 : 0)
                + (getShortenTemplates() ? 1 : 0)
                + getHighlightColorRGB()
                + ((int) getDelayMillis())
                + getCompilerTimeoutMillis()
                + (getInitialNoticeShown() ? 1 : 0)
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
                && getIgnoreSwitches().equals(other.getIgnoreSwitches())
                && getAutoscrollFromSource() == (other.getAutoscrollFromSource())
                && getAutoscrollToSource() == (other.getAutoscrollToSource())
                && getAutoupdateFromSource() == (other.getAutoupdateFromSource())
                && getShortenTemplates() == (other.getShortenTemplates())
                && getHighlightColorRGB() == other.getHighlightColorRGB()
                && getDelayMillis() == other.getDelayMillis()
                && getCompilerTimeoutMillis() == other.getCompilerTimeoutMillis()
                && getInitialNoticeShown() == other.getInitialNoticeShown()
        ;
    }
}
