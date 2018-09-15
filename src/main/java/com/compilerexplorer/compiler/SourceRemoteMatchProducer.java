package com.compilerexplorer.compiler;

import com.compilerexplorer.common.*;
import com.compilerexplorer.datamodel.SourceCompilerSettings;
import com.compilerexplorer.datamodel.SourceRemoteMatched;
import com.compilerexplorer.datamodel.state.*;
import com.google.common.annotations.VisibleForTesting;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class SourceRemoteMatchProducer implements Consumer<SourceCompilerSettings> {
    @NotNull
    private final Project project;
    @NotNull
    private final Consumer<SourceRemoteMatched> sourceRemoteMatchedConsumer;

    public SourceRemoteMatchProducer(@NotNull Project project_, @NotNull Consumer<SourceRemoteMatched> sourceRemoteMatchedConsumer_) {
        project = project_;
        sourceRemoteMatchedConsumer = sourceRemoteMatchedConsumer_;
    }

    @Override
    public void accept(@NotNull SourceCompilerSettings sourceCompilerSettings) {
        SettingsState state = SettingsProvider.getInstance(project).getState();

        if (!state.getEnabled()) {
            return;
        }

        {
            CompilerMatches existingMatches = state.getCompilerMatches().get(new LocalCompilerPath(sourceCompilerSettings.getSourceSettings().getCompiler().getAbsolutePath()));
            if (existingMatches != null) {
                sourceRemoteMatchedConsumer.accept(new SourceRemoteMatched(sourceCompilerSettings, existingMatches));
                return;
            }
        }

        String localName = sourceCompilerSettings.getLocalCompilerSettings().getName().toLowerCase();
        String localVersion = localName.equals("gcc")
                ? stripLastGCCVersionDigitIfNeeded(sourceCompilerSettings.getLocalCompilerSettings().getVersion())
                : sourceCompilerSettings.getLocalCompilerSettings().getVersion();
        String localTarget = sourceCompilerSettings.getLocalCompilerSettings().getTarget();
        String language = sourceCompilerSettings.getSourceSettings().getLanguage();
        List<CompilerMatch> remoteCompilerMatches = findRemoteCompilerMatches(state.getRemoteCompilers(), localName, localVersion, localTarget, language);
        CompilerMatches matches = new CompilerMatches(findBestMatch(remoteCompilerMatches), remoteCompilerMatches);
        sourceRemoteMatchedConsumer.accept(new SourceRemoteMatched(sourceCompilerSettings, matches));
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
