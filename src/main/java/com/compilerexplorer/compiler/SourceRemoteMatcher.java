package com.compilerexplorer.compiler;

import com.compilerexplorer.common.*;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class SourceRemoteMatcher implements SourceCompilerSettingsConsumer, RemoteConnectionConsumer {
    @NotNull
    private final Project project;
    @NotNull
    private final SourceRemoteMatchedConsumer sourceRemoteMatchedConsumer;
    private SourceCompilerSettings sourceCompilerSettings;
    private String reason;

    public SourceRemoteMatcher(@NotNull Project project_, @NotNull SourceRemoteMatchedConsumer sourceRemoteMatchedConsumer_) {
        project = project_;
        sourceRemoteMatchedConsumer = sourceRemoteMatchedConsumer_;
    }

    @Override
    public void setSourceCompilerSetting(@NotNull SourceCompilerSettings sourceCompilerSettings_) {
        sourceCompilerSettings = sourceCompilerSettings_;
        reason = null;
        refresh();
    }

    @Override
    public void clearSourceCompilerSetting(@NotNull String reason_) {
        sourceCompilerSettings = null;
        reason = reason_;
        refresh();
    }

    @Override
    public void connected() {
        refresh();
    }

    private void refresh() {
        if (reason != null) {
            sourceRemoteMatchedConsumer.clearSourceRemoteMatched(reason);
            return;
        }

        SettingsState state = SettingsProvider.getInstance(project).getState();
        if (!state.getConnected()) {
            sourceRemoteMatchedConsumer.clearSourceRemoteMatched("Not connected" + (state.getLastConnectionStatus().isEmpty() ? "" : "\n" + state.getLastConnectionStatus()));
            return;
        }

        if (sourceCompilerSettings == null) {
            sourceRemoteMatchedConsumer.clearSourceRemoteMatched("No source");
            return;
        }

        {
            List<String> existingMatches = state.getCompilerMatches().get(sourceCompilerSettings.getSourceSettings().getCompiler().getAbsolutePath());
            if (existingMatches != null) {
                sourceRemoteMatchedConsumer.setSourceRemoteMatched(new SourceRemoteMatched(sourceCompilerSettings, existingMatches));
            }
        }

        String localName = sourceCompilerSettings.getLocalCompilerSettings().getName();
        String localVersion = sourceCompilerSettings.getLocalCompilerSettings().getVersion();
        String localTarget = sourceCompilerSettings.getLocalCompilerSettings().getTarget();
        String language = sourceCompilerSettings.getSourceSettings().getLanguage().getDisplayName();
        List<String> remoteCompilerIds = findRemoteCompilerMatches(state.getRemoteCompilers(), localName, localVersion, localTarget, language, state.getAllowMinorVersionMismatch());
        if (remoteCompilerIds.isEmpty()) {
            String modifiedLocalVersion = stripLastVersionPart(localVersion);
            if (!modifiedLocalVersion.isEmpty()) {
                remoteCompilerIds.addAll(findRemoteCompilerMatches(state.getRemoteCompilers(), localName, modifiedLocalVersion, localTarget, language, state.getAllowMinorVersionMismatch()));
            }
        }
        remoteCompilerIds = remoteCompilerIds.stream().distinct().collect(Collectors.toList());
        sourceRemoteMatchedConsumer.setSourceRemoteMatched(new SourceRemoteMatched(sourceCompilerSettings, remoteCompilerIds));
    }

    @NotNull
    private static List<String> findRemoteCompilerMatches(@NotNull List<SettingsState.RemoteCompilerInfo> remoteCompilers,
                                                          @NotNull String localName,
                                                          @NotNull String localVersion,
                                                          @NotNull String localTarget,
                                                          @NotNull String language, boolean allowMinorVersionMismatch) {
        return remoteCompilers.stream()
                .filter(s -> s.getLanguage().toLowerCase().equals(language.toLowerCase()))
                .filter(s -> s.getName().replaceAll("-", "_").contains(localTarget.replaceAll("-", "_")))
                .filter(s -> s.getName().contains(localName))
                .filter(s -> versionMatches(s.getName(), localVersion, false) || (allowMinorVersionMismatch && versionMatches(s.getName(), localVersion, true)))
                .map(SettingsState.RemoteCompilerInfo::getId)
                .collect(Collectors.toList());
    }

    private static boolean versionMatches(@NotNull String remoteName, @NotNull String localVersion, boolean tryMinorMismatch) {
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
