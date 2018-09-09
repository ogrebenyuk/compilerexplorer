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
    private boolean connected = SettingsState.DEFAULT_CONNECTED;
    private boolean allowMinorVersionMismatch = SettingsState.DEFAULT_ALLOW_MINOR_VERSION_MISMATCH;
    @Nullable
    private List<RemoteCompilerInfo> remoteCompilers;

    public SourceRemoteMatcher(@NotNull Project project_, @NotNull SourceRemoteMatchedConsumer sourceRemoteMatchedConsumer_) {
        project = project_;
        sourceRemoteMatchedConsumer = sourceRemoteMatchedConsumer_;
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

    private void stateChanged(boolean force) {
        SettingsState state = SettingsProvider.getInstance(project).getState();
        boolean newConnected = state.getConnected();
        List<RemoteCompilerInfo> newRemoteCompilers = state.getRemoteCompilers();
        boolean newAllowMinorVersionMismatch = state.getAllowMinorVersionMismatch();
        boolean changed = connected != newConnected
                || !newRemoteCompilers.equals(remoteCompilers)
                || newAllowMinorVersionMismatch != allowMinorVersionMismatch;
        if (changed || force) {
            connected = newConnected;
            remoteCompilers = newRemoteCompilers;
            allowMinorVersionMismatch = newAllowMinorVersionMismatch;
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
        String localVersion = sourceCompilerSettings.getLocalCompilerSettings().getVersion();
        String localTarget = sourceCompilerSettings.getLocalCompilerSettings().getTarget();
        String language = sourceCompilerSettings.getSourceSettings().getLanguage().getDisplayName();
        List<CompilerMatch> remoteCompilerMatches = findRemoteCompilerMatches(remoteCompilers, localName, localVersion, localTarget, language, allowMinorVersionMismatch);
        if (remoteCompilerMatches.isEmpty()) {
            String modifiedLocalVersion = stripLastVersionPart(localVersion);
            if (!modifiedLocalVersion.isEmpty()) {
                remoteCompilerMatches.addAll(findRemoteCompilerMatches(remoteCompilers, localName, modifiedLocalVersion, localTarget, language, allowMinorVersionMismatch));
            }
        }
        remoteCompilerMatches = remoteCompilerMatches.stream().distinct().collect(Collectors.toList());
        CompilerMatches matches = new CompilerMatches(remoteCompilerMatches.isEmpty() ? new CompilerMatch() : new CompilerMatch(remoteCompilerMatches.get(0)), remoteCompilerMatches);
        sourceRemoteMatchedConsumer.setSourceRemoteMatched(new SourceRemoteMatched(sourceCompilerSettings, matches));
    }

    @NotNull
    private static List<CompilerMatch> findRemoteCompilerMatches(@NotNull List<RemoteCompilerInfo> remoteCompilers,
                                                                 @NotNull String localName,
                                                                 @NotNull String localVersion,
                                                                 @NotNull String localTarget,
                                                                 @NotNull String language,
                                                                 boolean allowMinorVersionMismatch) {
        return remoteCompilers.stream()
                .filter(s -> s.getLanguage().toLowerCase().equals(language.toLowerCase()))
                .filter(s -> s.getName().replaceAll("-", "_").contains(localTarget.replaceAll("-", "_")))
                .filter(s -> s.getName().contains(localName))
                .map(s -> findCompilerVersionMatch(s, localVersion, allowMinorVersionMismatch))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Nullable
    private static CompilerMatch findCompilerVersionMatch(@NotNull RemoteCompilerInfo remoteCompilerInfo, @NotNull String localVersion, boolean allowMinorVersionMismatch) {
        if (versionMatches(remoteCompilerInfo.getName(), localVersion, false)) {
            return new CompilerMatch(remoteCompilerInfo, CompilerMatchKind.EXACT_MATCH);
        } else if (allowMinorVersionMismatch && versionMatches(remoteCompilerInfo.getName(), localVersion, true)) {
            return new CompilerMatch(remoteCompilerInfo, CompilerMatchKind.MINOR_MISMATCH);
        }
        return null;
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
    private static String stripLastVersionPart(@NotNull String version) {
        return version.replaceAll("^(.*)\\.[^.]*$", "$1");
    }
}
