package com.compilerexplorer.compiler;

import com.compilerexplorer.common.*;
import com.compilerexplorer.common.datamodel.SourceCompilerSettings;
import com.compilerexplorer.common.datamodel.SourceCompilerSettingsConsumer;
import com.compilerexplorer.common.datamodel.SourceRemoteMatched;
import com.compilerexplorer.common.datamodel.SourceRemoteMatchedConsumer;
import com.compilerexplorer.common.datamodel.state.*;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class SourceRemoteMatcher implements SourceCompilerSettingsConsumer, StateConsumer, RemoteConnectionConsumer {
    @NotNull
    private final Project project;
    @NotNull
    private final SourceRemoteMatchedConsumer sourceRemoteMatchedConsumer;
    @Nullable
    private SourceCompilerSettings sourceCompilerSettings;
    @Nullable
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
    public void stateChanged() {
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
            CompilerMatches existingMatches = state.getCompilerMatches().get(new LocalCompilerPath(sourceCompilerSettings.getSourceSettings().getCompiler().getAbsolutePath()));
            if (existingMatches != null) {
                sourceRemoteMatchedConsumer.setSourceRemoteMatched(new SourceRemoteMatched(sourceCompilerSettings, existingMatches));
            }
        }

        String localName = sourceCompilerSettings.getLocalCompilerSettings().getName();
        String localVersion = sourceCompilerSettings.getLocalCompilerSettings().getVersion();
        String localTarget = sourceCompilerSettings.getLocalCompilerSettings().getTarget();
        String language = sourceCompilerSettings.getSourceSettings().getLanguage().getDisplayName();
        List<CompilerMatch> remoteCompilerMatches = findRemoteCompilerMatches(state.getRemoteCompilers(), localName, localVersion, localTarget, language, state.getAllowMinorVersionMismatch());
        if (remoteCompilerMatches.isEmpty()) {
            String modifiedLocalVersion = stripLastVersionPart(localVersion);
            if (!modifiedLocalVersion.isEmpty()) {
                remoteCompilerMatches.addAll(findRemoteCompilerMatches(state.getRemoteCompilers(), localName, modifiedLocalVersion, localTarget, language, state.getAllowMinorVersionMismatch()));
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
                .map(s -> findCompilerVersionMatch(s.getName(), s.getId(), localVersion, allowMinorVersionMismatch))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    @Nullable
    private static CompilerMatch findCompilerVersionMatch(@NotNull String remoteCompilerName, @NotNull String remoteCompilerId, @NotNull String localVersion, boolean allowMinorVersionMismatch) {
        if (versionMatches(remoteCompilerName, localVersion, false)) {
            return new CompilerMatch(new RemoteCompilerId(remoteCompilerId), CompilerMatchKind.EXACT_MATCH);
        } else if (allowMinorVersionMismatch && versionMatches(remoteCompilerName, localVersion, true)) {
            return new CompilerMatch(new RemoteCompilerId(remoteCompilerId), CompilerMatchKind.MINOR_MISMATCH);
        }
        return null;
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
