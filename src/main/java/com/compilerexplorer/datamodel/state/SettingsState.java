package com.compilerexplorer.datamodel.state;

import com.compilerexplorer.common.Constants;
import com.compilerexplorer.common.Tabs;
import com.intellij.util.xmlb.annotations.Property;
import com.intellij.util.xmlb.annotations.Transient;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.io.Serializable;
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
    private Map<String, List<RemoteLibraryInfo>> remoteLibraries = new HashMap<>();
    @NotNull
    @Property
    private Map<String, List<EnabledRemoteLibraryInfo>> enabledRemoteLibraries = new HashMap<>();
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
    private static class FileTabPair implements Serializable {
        @NotNull @Property final String file;
        @NotNull @Property final Tabs tab;
        FileTabPair() {file = ""; tab = Tabs.EVERYTHING;}
        FileTabPair(@NotNull String file_, @NotNull Tabs tab_) {file = file_; tab = tab_;}
        @Override public int hashCode() {return file.hashCode() + tab.hashCode();}
        @Override public boolean equals(Object obj) {
            if (!(obj instanceof FileTabPair other)) return false;
            return file.equals(other.file) && tab.equals(other.tab);
        }
    }
    @NotNull
    @Property
    private Map<FileTabPair, Set<String>> fileTabFoldedLabels = new HashMap<>();
    @Property
    private long delayMillis = DEFAULT_DELAY_MILLIS;
    @Property
    private int compilerTimeoutMillis = DEFAULT_COMPILER_TIMEOUT_MILLIS;
    @Property
    private boolean showAllTabs = false;
    @Property
    private Map<Tabs, Integer> scrollPositions = new HashMap<>();
    @Property
    private Map<Tabs, Integer> scrollPositionsError = new HashMap<>();
    @NotNull
    @Property
    private Tabs lastOpenTab = Tabs.EXPLORER_OUTPUT;
    @Property
    private boolean initialNoticeShown = false;

    public SettingsState() {
        // empty
    }

    synchronized public boolean getEnabled() {
        return enabled;
    }

    synchronized public void setEnabled(boolean enabled_) {
        enabled = enabled_;
    }

    @NotNull
    synchronized public String getUrl() {
        return url;
    }

    synchronized public void setUrl(@NotNull String url_) {
        url = url_;
    }

    @NotNull
    synchronized public List<String> getUrlHistory() {
        return new ArrayList<>(urlHistory);
    }

    synchronized public void setUrlHistory(@NotNull List<String> urlHistory_) {
        urlHistory = new ArrayList<>(urlHistory_);
    }

    synchronized public void addToUrlHistory(@NotNull String url_) {
        if (!Constants.DEFAULT_URLS.containsKey(url_) && !urlHistory.contains(url_)) {
            urlHistory.add(url_);
        }
    }

    synchronized public void clearUrlHistory() {
        urlHistory = EMPTY_URL_HISTORY;
    }

    synchronized public boolean getConnected() {
        return connected;
    }

    synchronized public void setConnected(boolean connected_) {
        connected = connected_;
    }

    @NotNull
    synchronized public List<RemoteCompilerInfo> getRemoteCompilers() {
        List<RemoteCompilerInfo> copy = new ArrayList<>();
        remoteCompilers.forEach(otherInfo -> copy.add(new RemoteCompilerInfo(otherInfo)));
        return copy;
    }

    synchronized public void setRemoteCompilers(@NotNull List<RemoteCompilerInfo> remoteCompilers_) {
        remoteCompilers = new ArrayList<>();
        remoteCompilers_.forEach(otherInfo -> remoteCompilers.add(new RemoteCompilerInfo(otherInfo)));
    }

    synchronized public void clearRemoteCompilers() {
        setRemoteCompilers(EMPTY.getRemoteCompilers());
    }

    @NotNull
    synchronized public Map<String, List<RemoteLibraryInfo>> getRemoteLibraries() {
        Map<String, List<RemoteLibraryInfo>> result = new HashMap<>();
        remoteLibraries.forEach((key, value) -> result.put(key, new ArrayList<>(value)));
        return result;
    }

    synchronized public void setRemoteLibraries(@NotNull Map<String, List<RemoteLibraryInfo>> remoteLibraries_) {
        remoteLibraries = new HashMap<>();
        remoteLibraries_.forEach((key, value) -> remoteLibraries.put(key, new ArrayList<>(value)));
    }

    synchronized public void clearRemoteLibraries() {
        setRemoteLibraries(EMPTY.getRemoteLibraries());
    }

    synchronized public void clearRemoteLibrariesForLanguage(@NonNls @NotNull String language) {
        remoteLibraries.remove(language);
    }

    synchronized public void setRemoteLibrariesForLanguage(@NonNls @NotNull String language, @NotNull List<RemoteLibraryInfo> libraries) {
        remoteLibraries.put(language, new ArrayList<>(libraries));
    }

    @NotNull
    synchronized public Set<String> getRemoteLibrariesLanguages() {
        return new HashSet<>(remoteLibraries.keySet());
    }

    @NotNull
    synchronized public Map<String, List<EnabledRemoteLibraryInfo>> getEnabledRemoteLibraries() {
        Map<String, List<EnabledRemoteLibraryInfo>> result = new HashMap<>();
        enabledRemoteLibraries.forEach((key, value) -> result.put(key, new ArrayList<>(value)));
        return result;
    }

    @NotNull
    synchronized public Optional<List<EnabledRemoteLibraryInfo>> getEnabledRemoteLibrariesForLanguage(@NonNls @NotNull String language) {
        @Nullable List<EnabledRemoteLibraryInfo> libraries = enabledRemoteLibraries.get(language);
        return Optional.ofNullable(libraries != null ? new ArrayList<>(libraries) : null);
    }

    synchronized public void setEnabledRemoteLibraries(@NotNull Map<String, List<EnabledRemoteLibraryInfo>> enabledRemoteLibraries_) {
        enabledRemoteLibraries = new HashMap<>();
        enabledRemoteLibraries_.forEach((key, value) -> enabledRemoteLibraries.put(key, new ArrayList<>(value)));
    }

    synchronized public void clearEnabledRemoteLibraries() {
        enabledRemoteLibraries = new HashMap<>();
    }

    @NotNull
    synchronized public Map<LocalCompilerPath, LocalCompilerSettings> getLocalCompilerSettings() {
        Map<LocalCompilerPath, LocalCompilerSettings> copy = new HashMap<>();
        localCompilerSettings.forEach((key, value) -> copy.put(new LocalCompilerPath(key), new LocalCompilerSettings(value)));
        return copy;
    }

    synchronized public void setLocalCompilerSettings(@NotNull Map<LocalCompilerPath, LocalCompilerSettings> localCompilers_) {
        localCompilerSettings = new HashMap<>();
        localCompilers_.forEach((key, value) -> localCompilerSettings.put(new LocalCompilerPath(key), new LocalCompilerSettings(value)));
    }

    synchronized public void addToLocalCompilerSettings(@NotNull LocalCompilerPath compilerPath, @NotNull LocalCompilerSettings compilerSettings) {
        localCompilerSettings.put(compilerPath, compilerSettings);
    }

    synchronized public void clearLocalCompilerSettings() {
        setLocalCompilerSettings(EMPTY.getLocalCompilerSettings());
    }

    @NotNull
    synchronized public Filters getFilters() {
        return new Filters(filters);
    }

    synchronized public void setFilters(@NotNull Filters filters_) {
        filters = new Filters(filters_);
    }

    @NotNull
    synchronized public Map<LocalCompilerPath, CompilerMatches> getCompilerMatches() {
        Map<LocalCompilerPath, CompilerMatches> copy = new HashMap<>();
        compilerMatches.forEach((key, value) -> copy.put(new LocalCompilerPath(key), new CompilerMatches(value)));
        return copy;
    }

    synchronized public void setCompilerMatches(@NotNull Map<LocalCompilerPath, CompilerMatches> compilerMatches_) {
        compilerMatches = new HashMap<>();
        compilerMatches_.forEach((key, value) -> compilerMatches.put(new LocalCompilerPath(key), new CompilerMatches(value)));
    }

    synchronized public void addToCompilerMatches(@NotNull LocalCompilerPath compilerPath, @NotNull CompilerMatches matches) {
        compilerMatches.put(compilerPath, matches);
    }

    synchronized public void clearCompilerMatches() {
        setCompilerMatches(EMPTY.getCompilerMatches());
    }

    synchronized public boolean getPreprocessLocally() {
        return preprocessLocally;
    }

    synchronized public void setPreprocessLocally(boolean preprocessLocally_) {
        preprocessLocally = preprocessLocally_;
    }

    @NotNull
    synchronized public String getAdditionalSwitches() {
        return additionalSwitches;
    }

    synchronized public void setAdditionalSwitches(@NotNull String additionalSwitches_) {
        additionalSwitches = additionalSwitches_;
    }

    @NotNull
    synchronized public String getIgnoreSwitches() {
        return ignoreSwitches;
    }

    synchronized public void setIgnoreSwitches(@NotNull String ignoreSwitches_) {
        ignoreSwitches = ignoreSwitches_;
    }

    synchronized public boolean getAutoscrollFromSource() {
        return autoscrollFromSource;
    }

    synchronized public void setAutoscrollFromSource(boolean autoscrollFromSource_) {
        autoscrollFromSource = autoscrollFromSource_;
    }

    synchronized public boolean getAutoscrollToSource() {
        return autoscrollToSource;
    }

    synchronized public void setAutoscrollToSource(boolean autoscrollToSource_) {
        autoscrollToSource = autoscrollToSource_;
    }

    synchronized public boolean getAutoupdateFromSource() {
        return autoupdateFromSource;
    }

    synchronized public void setAutoupdateFromSource(boolean autoupdateFromSource_) {
        autoupdateFromSource = autoupdateFromSource_;
    }

    synchronized public boolean getShortenTemplates() {
        return shortenTemplates;
    }

    synchronized public void setShortenTemplates(boolean shortenTemplates_) {
        shortenTemplates = shortenTemplates_;
    }

    synchronized public boolean getShowLineNumbers() {
        return showLineNumbers;
    }

    synchronized public void setShowLineNumbers(boolean showLineNumbers_) {
        showLineNumbers = showLineNumbers_;
    }

    synchronized public boolean getShowByteOffsets() {
        return showByteOffsets;
    }

    synchronized public void setShowByteOffsets(boolean showByteOffsets_) {
        showByteOffsets = showByteOffsets_;
    }

    synchronized public boolean getShowSourceAnnotations() {
        return showSourceAnnotations;
    }

    synchronized public void setShowSourceAnnotations(boolean showSourceAnnotations_) {
        showSourceAnnotations = showSourceAnnotations_;
    }

    synchronized public boolean getShowOpcodes() {
        return showOpcodes;
    }

    synchronized public void setShowOpcodes(boolean showOpcodes_) {
        showOpcodes = showOpcodes_;
    }

    synchronized public boolean getEnableFolding() {
        return enableFolding;
    }

    synchronized public void setEnableFolding(boolean enableFolding_) {
        enableFolding = enableFolding_;
    }

    @NotNull
    synchronized public Map<FileTabPair, Set<String>> getFoldedLabels() {
        Map<FileTabPair, Set<String>> copy = new HashMap<>();
        fileTabFoldedLabels.forEach((key, value) -> copy.put(new FileTabPair(key.file, key.tab), new HashSet<>(value)));
        return copy;
    }

    synchronized public void setFoldedLabels(@NotNull Map<FileTabPair, Set<String>> foldedLabels_) {
        fileTabFoldedLabels = new HashMap<>();
        foldedLabels_.forEach((key, value) -> fileTabFoldedLabels.put(new FileTabPair(key.file, key.tab), new HashSet<>(value)));
    }

    @Nullable
    synchronized public Set<String> findFoldedLabels(@NotNull String file, @NotNull Tabs tab) {
        @Nullable Set<String> labels = fileTabFoldedLabels.get(new FileTabPair(file, tab));
        return labels != null ? new HashSet<>(labels) : null;
    }

    synchronized public void clearFoldedLabels(@NotNull String file, @NotNull Tabs tab) {
        fileTabFoldedLabels.remove(new FileTabPair(file, tab));
    }

    synchronized public void addFoldedLabel(@NotNull String file, @NotNull Tabs tab, @NotNull String label) {
        FileTabPair key = new FileTabPair(file, tab);
        @Nullable Set<String> labels = fileTabFoldedLabels.get(key);
        if (labels != null) {
            labels.add(label);
        } else {
            fileTabFoldedLabels.put(key, new HashSet<>(List.of(label)));
        }
    }

    synchronized public void removeFoldedLabel(@NotNull String file, @NotNull Tabs tab, @NotNull String label) {
        FileTabPair key = new FileTabPair(file, tab);
        @Nullable Set<String> labels = fileTabFoldedLabels.get(key);
        if (labels != null) {
            labels.remove(label);
            if (labels.isEmpty()) {
                fileTabFoldedLabels.remove(key);
            }
        }
    }

    synchronized public long getDelayMillis() {
        return delayMillis;
    }

    synchronized public void setDelayMillis(long delayMillis_) {
        delayMillis = delayMillis_;
    }

    synchronized public int getCompilerTimeoutMillis() {
        return compilerTimeoutMillis;
    }

    synchronized public void setCompilerTimeoutMillis(int compilerTimeoutMillis_) {
        compilerTimeoutMillis = compilerTimeoutMillis_;
    }

    synchronized public boolean getShowAllTabs() {
        return showAllTabs;
    }

    synchronized public void setShowAllTabs(boolean showAllTabs_) {
        showAllTabs = showAllTabs_;
    }

    @NotNull
    synchronized public Map<Tabs, Integer> getScrollPositions() {
        return new HashMap<>(scrollPositions);
    }

    synchronized public void setScrollPositions(@NotNull Map<Tabs, Integer> scrollPositions_) {
        scrollPositions = new HashMap<>(scrollPositions_);
    }

    synchronized public void addToScrollPositions(@NotNull Tabs tab, int position) {
        scrollPositions.put(tab, position);
    }

    @NotNull
    synchronized public Map<Tabs, Integer> getScrollPositionsError() {
        return new HashMap<>(scrollPositionsError);
    }

    synchronized public void setScrollPositionsError(@NotNull Map<Tabs, Integer> scrollPositionsError_) {
        scrollPositionsError = new HashMap<>(scrollPositionsError_);
    }

    synchronized public void addToScrollPositionsError(@NotNull Tabs tab, int position) {
        scrollPositionsError.put(tab, position);
    }

    @NotNull
    synchronized public Tabs getLastOpenTab() {
        return lastOpenTab;
    }

    synchronized public void setLastOpenTab(@NotNull Tabs tab) {
        lastOpenTab = tab;
    }

    synchronized public boolean getInitialNoticeShown() {
        return initialNoticeShown;
    }

    synchronized public void setInitialNoticeShown(boolean initialNoticeShown_) {
        initialNoticeShown = initialNoticeShown_;
    }

    public void copyFrom(@NotNull SettingsState other) {
        setEnabled(other.getEnabled());
        setUrl(other.getUrl());
        setUrlHistory(other.getUrlHistory());
        setConnected(other.getConnected());
        setRemoteCompilers(other.getRemoteCompilers());
        setRemoteLibraries(other.getRemoteLibraries());
        setEnabledRemoteLibraries(other.getEnabledRemoteLibraries());
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
        setDelayMillis(other.getDelayMillis());
        setCompilerTimeoutMillis(other.getCompilerTimeoutMillis());
        setShowAllTabs(other.getShowAllTabs());
        setScrollPositions(other.getScrollPositions());
        setScrollPositionsError(other.getScrollPositionsError());
        setLastOpenTab(other.getLastOpenTab());
        setInitialNoticeShown(other.getInitialNoticeShown());
    }

    @Override
    public int hashCode() {
        return (getEnabled() ? 1 : 0)
                + getUrl().hashCode()
                + getUrlHistory().hashCode()
                + (getConnected() ? 1 : 0)
                + getRemoteCompilers().hashCode()
                + getRemoteLibraries().hashCode()
                + getEnabledRemoteLibraries().hashCode()
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
                + ((int) getDelayMillis())
                + getCompilerTimeoutMillis()
                + (getShowAllTabs() ? 1 : 0)
                + getScrollPositions().hashCode()
                + getScrollPositionsError().hashCode()
                + getLastOpenTab().hashCode()
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
                && getRemoteLibraries().equals(other.getRemoteLibraries())
                && getEnabledRemoteLibraries().equals(other.getEnabledRemoteLibraries())
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
                && getDelayMillis() == other.getDelayMillis()
                && getCompilerTimeoutMillis() == other.getCompilerTimeoutMillis()
                && getShowAllTabs() == other.getShowAllTabs()
                && getScrollPositions().equals(other.getScrollPositions())
                && getScrollPositionsError().equals(other.getScrollPositionsError())
                && getLastOpenTab().equals(other.getLastOpenTab())
                && getInitialNoticeShown() == other.getInitialNoticeShown()
        ;
    }
}
