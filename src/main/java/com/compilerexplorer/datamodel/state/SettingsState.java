package com.compilerexplorer.datamodel.state;

import com.compilerexplorer.common.Constants;
import com.compilerexplorer.common.Tabs;
import com.intellij.util.xmlb.annotations.Property;
import com.intellij.util.xmlb.annotations.Transient;
import org.jetbrains.annotations.NotNull;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.util.*;

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
    private static final long DEFAULT_DELAY_MILLIS = Constants.DEFAULT_DELAY_MILLIS;
    private static final int DEFAULT_COMPILER_TIMEOUT_MILLIS = Constants.DEFAULT_COMPILER_TIMEOUT_MILLIS;
    @NotNull
    private static final List<String> EMPTY_URL_HISTORY = new ArrayList<>();

    public static final int NO_SAVED_COLOR = -1;

    @NotNull
    public static final SettingsState EMPTY = new SettingsState();

    @Transient
    private boolean enabled = true;
    @NotNull
    @Property
    private String url = DEFAULT_URL;
    @NotNull
    @Property
    private List<String> urlHistory = EMPTY_URL_HISTORY;
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
    private boolean showLineNumbers = false;
    @Property
    private boolean showByteOffsets = false;
    @Property
    private boolean showSourceAnnotations = false;
    @Property
    private boolean showOpcodes = true;
    @Property
    private boolean enableFolding = false;
    @NotNull
    @Property
    private Set<String> foldedLabels = new HashSet<>();
    @Property
    private int highlightColorRGB = NO_SAVED_COLOR;
    @Property
    private long delayMillis = DEFAULT_DELAY_MILLIS;
    @Property
    private int compilerTimeoutMillis = DEFAULT_COMPILER_TIMEOUT_MILLIS;
    @Property
    private boolean showAllTabs = false;
    @Property
    private Map<Tabs, Integer> scrollPositions = new HashMap<>();
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

    @NotNull
    public List<String> getUrlHistory() {
        return urlHistory;
    }

    public void setUrlHistory(@NotNull List<String> urlHistory_) {
        urlHistory = new ArrayList<>();
        urlHistory.addAll(urlHistory_);
    }

    public void addToUrlHistory(@NotNull String url_) {
        if (!Constants.DEFAULT_URLS.containsKey(url_) && !urlHistory.contains(url_)) {
            urlHistory.add(url_);
        }
    }

    public void clearUrlHistory() {
        urlHistory = EMPTY_URL_HISTORY;
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

    public boolean getShowLineNumbers() {
        return showLineNumbers;
    }

    public void setShowLineNumbers(boolean showLineNumbers_) {
        showLineNumbers = showLineNumbers_;
    }

    public boolean getShowByteOffsets() {
        return showByteOffsets;
    }

    public void setShowByteOffsets(boolean showByteOffsets_) {
        showByteOffsets = showByteOffsets_;
    }

    public boolean getShowSourceAnnotations() {
        return showSourceAnnotations;
    }

    public void setShowSourceAnnotations(boolean showSourceAnnotations_) {
        showSourceAnnotations = showSourceAnnotations_;
    }

    public boolean getShowOpcodes() {
        return showOpcodes;
    }

    public void setShowOpcodes(boolean showOpcodes_) {
        showOpcodes = showOpcodes_;
    }

    public boolean getEnableFolding() {
        return enableFolding;
    }

    public void setEnableFolding(boolean enableFolding_) {
        enableFolding = enableFolding_;
    }

    @NotNull
    public Set<String> getFoldedLabels() {
        return foldedLabels;
    }

    public void setFoldedLabels(@NotNull Set<String> foldedLabels_) {
        foldedLabels = new HashSet<>();
        foldedLabels.addAll(foldedLabels_);
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

    public boolean getShowAllTabs() {
        return showAllTabs;
    }

    public void setShowAllTabs(boolean showAllTabs_) {
        showAllTabs = showAllTabs_;
    }

    @NotNull
    public Map<Tabs, Integer> getScrollPositions() {
        return scrollPositions;
    }

    public void setScrollPositions(@NotNull Map<Tabs, Integer> scrollPositions_) {
        scrollPositions = new HashMap<>();
        scrollPositions.putAll(scrollPositions_);
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
        setUrlHistory(other.getUrlHistory());
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
        setShowLineNumbers(other.getShowLineNumbers());
        setShowByteOffsets(other.getShowByteOffsets());
        setShowSourceAnnotations(other.getShowSourceAnnotations());
        setShowOpcodes(other.getShowOpcodes());
        setEnableFolding(other.getEnableFolding());
        setFoldedLabels(other.getFoldedLabels());
        setHighlightColorRGB(other.getHighlightColorRGB());
        setDelayMillis(other.getDelayMillis());
        setCompilerTimeoutMillis(other.getCompilerTimeoutMillis());
        setShowAllTabs(other.getShowAllTabs());
        setScrollPositions(other.getScrollPositions());
        setInitialNoticeShown(other.getInitialNoticeShown());
    }

    @Override
    public int hashCode() {
        return (getEnabled() ? 1 : 0)
                + getUrl().hashCode()
                + getUrlHistory().hashCode()
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
                + (getShowLineNumbers() ? 1 : 0)
                + (getShowByteOffsets() ? 1 : 0)
                + (getShowSourceAnnotations() ? 1 : 0)
                + (getShowOpcodes() ? 1 : 0)
                + (getEnableFolding() ? 1 : 0)
                + getFoldedLabels().hashCode()
                + getHighlightColorRGB()
                + ((int) getDelayMillis())
                + getCompilerTimeoutMillis()
                + (getShowAllTabs() ? 1 : 0)
                + getScrollPositions().hashCode()
                + (getInitialNoticeShown() ? 1 : 0)
        ;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof SettingsState other)) {
            return false;
        }
        return getEnabled() == other.getEnabled()
                && getUrl().equals(other.getUrl())
                && getUrlHistory().equals(other.getUrlHistory())
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
                && getShowLineNumbers() == (other.getShowLineNumbers())
                && getShowByteOffsets() == (other.getShowByteOffsets())
                && getShowSourceAnnotations() == (other.getShowSourceAnnotations())
                && getShowOpcodes() == (other.getShowOpcodes())
                && getEnableFolding() == (other.getEnableFolding())
                && getFoldedLabels().equals(other.getFoldedLabels())
                && getHighlightColorRGB() == other.getHighlightColorRGB()
                && getDelayMillis() == other.getDelayMillis()
                && getCompilerTimeoutMillis() == other.getCompilerTimeoutMillis()
                && getShowAllTabs() == other.getShowAllTabs()
                && getScrollPositions().equals(other.getScrollPositions())
                && getInitialNoticeShown() == other.getInitialNoticeShown()
        ;
    }
}
