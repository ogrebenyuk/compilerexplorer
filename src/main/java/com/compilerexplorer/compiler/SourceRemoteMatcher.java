package com.compilerexplorer.compiler;

import com.compilerexplorer.common.*;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.List;

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

        String localName = sourceCompilerSettings.getLocalCompilerSettings().getName();
        String localVersion = sourceCompilerSettings.getLocalCompilerSettings().getVersion();
        String localTarget = sourceCompilerSettings.getLocalCompilerSettings().getTarget();
        String language = sourceCompilerSettings.getSourceSettings().getLanguage().getDisplayName();
        String remoteCompilerId = findRemoteCompilerMatch(state.getRemoteCompilers(), localName, localVersion, localTarget, language);
        if (remoteCompilerId.isEmpty()) {
            String modifiedLocalVersion = stripLastVersionPart(localVersion);
            if (!modifiedLocalVersion.isEmpty()) {
                remoteCompilerId = findRemoteCompilerMatch(state.getRemoteCompilers(), localName, modifiedLocalVersion, localTarget, language);
            }
        }
        if (!remoteCompilerId.isEmpty()) {
            sourceRemoteMatchedConsumer.setSourceRemoteMatched(new SourceRemoteMatched(sourceCompilerSettings, remoteCompilerId));
        } else {
            sourceRemoteMatchedConsumer.clearSourceRemoteMatched("Cannot find matching remote compiler for local " + localTarget + " " + localName + " " + localVersion + " " + language + " compiler");
        }
    }

    @NotNull
    private static String findRemoteCompilerMatch(@NotNull List<SettingsState.RemoteCompilerInfo> remoteCompilers, @NotNull String localName, @NotNull String localVersion, @NotNull String localTarget, @NotNull String language) {
        return remoteCompilers.stream()
                .filter(s -> s.getLanguage().toLowerCase().equals(language.toLowerCase()))
                .filter(s -> s.getName().replaceAll("-", "_").contains(localTarget.replaceAll("-", "_")))
                .filter(s -> s.getName().contains(localName))
                .filter(s -> s.getName().contains(" " + localVersion + " ") || s.getName().endsWith(" " + localVersion))
                .map(SettingsState.RemoteCompilerInfo::getId)
                .findFirst()
                .orElse("");
    }

    @NotNull
    private static String stripLastVersionPart(@NotNull String version) {
        return version.replaceAll("^(.*)\\.[^.]*$", "$1");
    }
}
