package com.compilerexplorer.compiler;

import com.compilerexplorer.common.*;
import com.compilerexplorer.common.datamodel.SourceCompilerSettings;
import com.compilerexplorer.common.datamodel.SourceCompilerSettingsConsumer;
import com.compilerexplorer.common.datamodel.SourceRemoteMatched;
import com.compilerexplorer.common.datamodel.SourceRemoteMatchedConsumer;
import com.compilerexplorer.common.datamodel.state.*;
import com.google.common.annotations.VisibleForTesting;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class SourceRemoteMatcher implements SourceCompilerSettingsConsumer, StateConsumer {
    @NotNull
    private final Project project;
    @NotNull
    private final SourceRemoteMatchedConsumer sourceRemoteMatchedConsumer;
    @Nullable
    private SourceCompilerSettings sourceCompilerSettings;
    @Nullable
    private String reason;
    private boolean connected;
    @Nullable
    private List<RemoteCompilerInfo> remoteCompilers;

    public SourceRemoteMatcher(@NotNull Project project_, @NotNull SourceRemoteMatchedConsumer sourceRemoteMatchedConsumer_) {
        project = project_;
        sourceRemoteMatchedConsumer = sourceRemoteMatchedConsumer_;
        reset();
    }

    @Override
    public void setSourceCompilerSetting(@NotNull SourceCompilerSettings sourceCompilerSettings_) {
        if (!sourceCompilerSettings_.equals(sourceCompilerSettings)) {
            sourceCompilerSettings = sourceCompilerSettings_;
            reason = null;
            stateChanged(true);
        }
    }

    @Override
    public void clearSourceCompilerSetting(@NotNull String reason_) {
        if (!reason_.equals(reason)) {
            sourceCompilerSettings = null;
            reason = reason_;
            stateChanged(true);
        }
    }

    @Override
    public void stateChanged() {
        stateChanged(false);
    }

    @Override
    public void reset() {
        sourceCompilerSettings = null;
        reason = null;
        connected = SettingsState.DEFAULT_CONNECTED;
        remoteCompilers = null;
    }

    private void stateChanged(boolean force) {
        SettingsState state = SettingsProvider.getInstance(project).getState();
        boolean newConnected = state.getConnected();
        List<RemoteCompilerInfo> newRemoteCompilers = state.getRemoteCompilers();
        boolean changed = connected != newConnected
                || !newRemoteCompilers.equals(remoteCompilers)
                ;
        if (changed || force) {
            connected = newConnected;
            remoteCompilers = newRemoteCompilers;
            refresh();
        }
    }

    private void refresh() {
        if (reason != null) {
            sourceRemoteMatchedConsumer.clearSourceRemoteMatched(reason);
            return;
        }

        SettingsState state = SettingsProvider.getInstance(project).getState();
        if (sourceCompilerSettings == null) {
            sourceRemoteMatchedConsumer.clearSourceRemoteMatched("No source");
            return;
        }

        {
            CompilerMatches existingMatches = state.getCompilerMatches().get(new LocalCompilerPath(sourceCompilerSettings.getSourceSettings().getCompiler().getAbsolutePath()));
            if (existingMatches != null) {
                sourceRemoteMatchedConsumer.setSourceRemoteMatched(new SourceRemoteMatched(sourceCompilerSettings, existingMatches));
                return;
            }
        }

        if (!connected) {
            sourceRemoteMatchedConsumer.clearSourceRemoteMatched("Not connected" + (state.getLastConnectionStatus().isEmpty() ? "" : "\n" + state.getLastConnectionStatus()));
            return;
        }

        String localName = sourceCompilerSettings.getLocalCompilerSettings().getName();
        String localVersion = sourceCompilerSettings.getLocalCompilerSettings().getName().toLowerCase().equals("gcc")
                ? stripLastGCCVersionDigitIfNeeded(sourceCompilerSettings.getLocalCompilerSettings().getVersion())
                : sourceCompilerSettings.getLocalCompilerSettings().getVersion();
        String localTarget = sourceCompilerSettings.getLocalCompilerSettings().getTarget();
        String language = sourceCompilerSettings.getSourceSettings().getLanguage().getDisplayName();
        List<CompilerMatch> remoteCompilerMatches = findRemoteCompilerMatches(remoteCompilers, localName, localVersion, localTarget, language);
        CompilerMatches matches = new CompilerMatches(findBestMatch(remoteCompilerMatches), remoteCompilerMatches);
        sourceRemoteMatchedConsumer.setSourceRemoteMatched(new SourceRemoteMatched(sourceCompilerSettings, matches));
    }

    @NotNull
    private static List<CompilerMatch> findRemoteCompilerMatches(@NotNull List<RemoteCompilerInfo> remoteCompilers,
                                                                 @NotNull String localName,
                                                                 @NotNull String localVersion,
                                                                 @NotNull String localTarget,
                                                                 @NotNull String language) {
        return remoteCompilers.stream()
                .filter(s -> s.getLanguage().toLowerCase().equals(language.toLowerCase()))
                .map(s -> findCompilerVersionMatch(s, localName, localVersion, localTarget))
                .collect(Collectors.toList());
    }

    @NotNull
    private static CompilerMatch findBestMatch(@NotNull List<CompilerMatch> matches) {
        return matches.stream().filter(m -> m.getCompilerMatchKind() == CompilerMatchKind.EXACT_MATCH).findFirst().orElse(
                matches.stream().filter(m -> m.getCompilerMatchKind() == CompilerMatchKind.MINOR_MISMATCH).findFirst().orElse(
                        new CompilerMatch()
                )
        );
    }

    @NotNull
    private static CompilerMatch findCompilerVersionMatch(@NotNull RemoteCompilerInfo remoteCompilerInfo,
                                                          @NotNull String localName,
                                                          @NotNull String localVersion,
                                                          @NotNull String localTarget) {
        boolean targetMatches = remoteCompilerInfo.getName().replaceAll("-", "_").contains(localTarget.replaceAll("-", "_"));
        boolean nameMatches = remoteCompilerInfo.getName().toLowerCase().contains(localName.toLowerCase());
        if (targetMatches && nameMatches) {
            if (versionMatches(remoteCompilerInfo.getName(), localVersion, false)) {
                return new CompilerMatch(remoteCompilerInfo, CompilerMatchKind.EXACT_MATCH);
            } else if (versionMatches(remoteCompilerInfo.getName(), localVersion, true)) {
                return new CompilerMatch(remoteCompilerInfo, CompilerMatchKind.MINOR_MISMATCH);
            }
        }
        return new CompilerMatch(remoteCompilerInfo, CompilerMatchKind.NO_MATCH);
    }

    @VisibleForTesting
    public static boolean versionMatches(@NotNull String remoteName, @NotNull String localVersion, boolean tryMinorMismatch) {
        String localVersionRegex = "^.* " + (tryMinorMismatch ? (regexize(stripLastVersionPart(localVersion)) + "\\.[0-9]+") : regexize(localVersion)) + "( .*)?$";
        return remoteName.matches(localVersionRegex);
    }

    @NotNull
    private static String regexize(@NotNull String version) {
        return version.replaceAll("\\.", "\\\\.");
    }

    @NotNull
    private static String stripLastGCCVersionDigitIfNeeded(@NotNull String version) {
        String[] parts = version.split("\\.");
        if (parts.length == 3 && Long.valueOf(parts[0]) >= 5) {
            return version.replaceAll("^(.*)\\.[^.]*$", "$1");
        } else {
            return version;
        }
    }

    @NotNull
    private static String stripLastVersionPart(@NotNull String version) {
        return version.replaceAll("^(.*)\\.[^.]*$", "$1");
    }
}
