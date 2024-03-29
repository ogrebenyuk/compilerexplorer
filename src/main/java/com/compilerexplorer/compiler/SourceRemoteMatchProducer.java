package com.compilerexplorer.compiler;

import com.compilerexplorer.common.*;
import com.compilerexplorer.common.component.BaseComponent;
import com.compilerexplorer.common.component.CEComponent;
import com.compilerexplorer.common.component.DataHolder;
import com.compilerexplorer.datamodel.SelectedSource;
import com.compilerexplorer.datamodel.SelectedSourceCompiler;
import com.compilerexplorer.datamodel.SourceRemoteMatched;
import com.compilerexplorer.datamodel.state.*;
import com.google.common.annotations.VisibleForTesting;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.eclipse.lsp4j.jsonrpc.validation.NonNull;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.stream.Collectors;

public class SourceRemoteMatchProducer extends BaseComponent {
    @NonNls
    private static final Logger LOG = Logger.getInstance(SourceRemoteMatchProducer.class);
    @NonNls
    @NonNull
    private static final String GCC_NAME = "gcc";

    @NotNull
    private final Project project;

    public SourceRemoteMatchProducer(@NotNull CEComponent nextComponent, @NotNull Project project_) {
        super(nextComponent);
        LOG.debug("created");

        project = project_;
    }

    @Override
    protected void doClear(@NotNull DataHolder data) {
        LOG.debug("doClear");
        data.remove(SourceRemoteMatched.KEY);
    }

    @Override
    protected void doReset() {
        LOG.debug("doReset");
        SettingsState state = CompilerExplorerSettingsProvider.getInstance(project).getState();
        state.clearCompilerMatches();
    }

    @Override
    protected void doRefresh(@NotNull DataHolder data) {
        LOG.debug("doRefresh");
        SettingsState state = CompilerExplorerSettingsProvider.getInstance(project).getState();
        data.get(SelectedSource.KEY).ifPresentOrElse(selectedSource -> {
            CompilerMatches existingMatches = state.getCompilerMatches().get(new LocalCompilerPath(selectedSource.getSelectedSource().compilerPath));
            if (existingMatches != null) {
                LOG.debug("found cached " + selectedSource.getSelectedSource().compilerPath + " -> " + existingMatches.getChosenMatch().getRemoteCompilerInfo().getName());
                data.put(SourceRemoteMatched.KEY, new SourceRemoteMatched(true, existingMatches));
            } else {
                data.get(SelectedSourceCompiler.KEY).flatMap(SelectedSourceCompiler::getLocalCompilerSettings).ifPresentOrElse(localCompilerSettings -> {
                    String localName = localCompilerSettings.getName().toLowerCase();
                    String localVersionFull = localCompilerSettings.getVersion();
                    String localVersion = localName.equals(GCC_NAME) ? stripLastGCCVersionDigitIfNeeded(localVersionFull) : localVersionFull;
                    String localTarget = localCompilerSettings.getTarget();
                    String sourceLanguage = selectedSource.getSelectedSource().language;
                    List<CompilerMatch> remoteCompilerMatches = findRemoteCompilerMatches(state.getRemoteCompilers(), localName, localVersion, localVersionFull, localTarget, sourceLanguage);
                    data.put(SourceRemoteMatched.KEY, new SourceRemoteMatched(false, new CompilerMatches(findBestMatch(remoteCompilerMatches), remoteCompilerMatches)));
                }, () -> LOG.debug("cannot find input: local compiler"));
            }
        }, () -> LOG.debug("cannot find input: source"));
    }

    @NotNull
    private static List<CompilerMatch> findRemoteCompilerMatches(@NotNull List<RemoteCompilerInfo> remoteCompilers,
                                                                 @NotNull String localName,
                                                                 @NotNull String localVersion,
                                                                 @NotNull String localVersionFull,
                                                                 @NotNull String localTarget,
                                                                 @NotNull String sourceLanguage) {
        return remoteCompilers.stream()
                .filter(s -> LanguageUtil.isSourceLanguageCompatibleWithRemote(sourceLanguage, s.getLanguage()))
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
        boolean targetMatches = localTarget.isEmpty() ||
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
