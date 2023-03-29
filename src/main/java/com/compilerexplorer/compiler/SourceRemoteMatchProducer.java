package com.compilerexplorer.compiler;

import com.compilerexplorer.common.*;
import com.compilerexplorer.datamodel.PreprocessedSource;
import com.compilerexplorer.datamodel.SourceRemoteMatched;
import com.compilerexplorer.datamodel.state.*;
import com.google.common.annotations.VisibleForTesting;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class SourceRemoteMatchProducer implements Consumer<PreprocessedSource> {
    @NotNull
    private final Project project;
    @NotNull
    private final Consumer<SourceRemoteMatched> sourceRemoteMatchedConsumer;

    public SourceRemoteMatchProducer(@NotNull Project project_, @NotNull Consumer<SourceRemoteMatched> sourceRemoteMatchedConsumer_) {
        project = project_;
        sourceRemoteMatchedConsumer = sourceRemoteMatchedConsumer_;
    }

    @Override
    public void accept(@NotNull PreprocessedSource preprocessedSource) {
        SettingsState state = CompilerExplorerSettingsProvider.getInstance(project).getState();

        if (!state.getEnabled()) {
            return;
        }

        SourceRemoteMatched sourceRemoteMatched = new SourceRemoteMatched(preprocessedSource);

        if (preprocessedSource.isValid() && preprocessedSource.sourceCompilerSettings.sourceSettingsConnected.sourceSettings.isValid()) {
            assert preprocessedSource.sourceCompilerSettings.sourceSettingsConnected.sourceSettings.selectedSourceSettings != null;

            CompilerMatches existingMatches = state.getCompilerMatches().get(new LocalCompilerPath(preprocessedSource.sourceCompilerSettings.sourceSettingsConnected.sourceSettings.selectedSourceSettings.compilerPath));
            if (existingMatches != null) {
                sourceRemoteMatched.remoteCompilerMatches = existingMatches;
                sourceRemoteMatched.cached = true;
            } else {
                if (preprocessedSource.sourceCompilerSettings.isValid() && preprocessedSource.sourceCompilerSettings.localCompilerSettings != null) {
                    String localName = preprocessedSource.sourceCompilerSettings.localCompilerSettings.getName().toLowerCase();
                    String localVersionFull = preprocessedSource.sourceCompilerSettings.localCompilerSettings.getVersion();
                    String localVersion = localName.equals("gcc") ? stripLastGCCVersionDigitIfNeeded(localVersionFull) : localVersionFull;
                    String localTarget = preprocessedSource.sourceCompilerSettings.localCompilerSettings.getTarget();
                    String language = preprocessedSource.sourceCompilerSettings.sourceSettingsConnected.sourceSettings.selectedSourceSettings.language;
                    List<CompilerMatch> remoteCompilerMatches = findRemoteCompilerMatches(state.getRemoteCompilers(), localName, localVersion, localVersionFull, localTarget, language);
                    sourceRemoteMatched.remoteCompilerMatches = new CompilerMatches(findBestMatch(remoteCompilerMatches), remoteCompilerMatches);
                }
            }
        }

        sourceRemoteMatchedConsumer.accept(sourceRemoteMatched);
    }

    @NotNull
    private static List<CompilerMatch> findRemoteCompilerMatches(@NotNull List<RemoteCompilerInfo> remoteCompilers,
                                                                 @NotNull String localName,
                                                                 @NotNull String localVersion,
                                                                 @NotNull String localVersionFull,
                                                                 @NotNull String localTarget,
                                                                 @NotNull String language) {
        return remoteCompilers.stream()
                .filter(s -> s.getLanguage().equalsIgnoreCase(language))
                .map(s -> findCompilerVersionMatch(s, localName, localVersion, localVersionFull, localTarget))
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
                                                          @NotNull String localVersionFull,
                                                          @NotNull String localTarget) {
        String remoteVersion = remoteCompilerInfo.getVersion();
        String remoteName = remoteCompilerInfo.getName();
        boolean targetMatches =
                remoteVersion.replaceAll("-", "_").contains(localTarget.replaceAll("-", "_")) ||
                remoteName.replaceAll("-", "_").contains(localTarget.replaceAll("-", "_"));
        boolean nameMatches =
                remoteVersion.toLowerCase().contains(localName.toLowerCase()) ||
                remoteName.toLowerCase().contains(localName.toLowerCase());
        if (targetMatches && nameMatches) {
            if (versionMatches(remoteVersion, localVersionFull, false) || versionMatches(remoteName, localVersion, false)) {
                return new CompilerMatch(remoteCompilerInfo, CompilerMatchKind.EXACT_MATCH);
            } else if (versionMatches(remoteVersion, localVersionFull, true) || versionMatches(remoteName, localVersion, true)) {
                return new CompilerMatch(remoteCompilerInfo, CompilerMatchKind.MINOR_MISMATCH);
            }
        }
        return new CompilerMatch(remoteCompilerInfo, CompilerMatchKind.NO_MATCH);
    }

    @VisibleForTesting
    static boolean versionMatches(@NotNull String remoteName, @NotNull String localVersion, boolean tryMinorMismatch) {
        String localVersionRegex = "^.* " + (tryMinorMismatch ? (turnIntoRegex(stripLastVersionPart(localVersion)) + "\\.[0-9]+") : turnIntoRegex(localVersion)) + "( .*)?$";
        return remoteName.matches(localVersionRegex);
    }

    @NotNull
    private static String turnIntoRegex(@NotNull String version) {
        return version.replaceAll("\\.", "\\\\.");
    }

    @NotNull
    private static String stripLastGCCVersionDigitIfNeeded(@NotNull String version) {
        String[] parts = version.split("\\.");
        if (parts.length == 3 && Long.parseLong(parts[0]) >= 5) {
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
