package com.compilerexplorer.common.compilerkind;

import com.jetbrains.cidr.lang.OCLanguageKind;
import com.jetbrains.cidr.lang.workspace.compiler.GCCCompiler;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public class GccLikeCompilerKind implements CompilerKind {
    @NonNls
    @NotNull
    private static final String VERSION_OPTION = "-v";
    @NonNls
    @NotNull
    private static final List<String> ADDITIONAL_SWITCHES = Collections.singletonList("-Wno-pedantic");

    @NonNls
    @NotNull
    private final String compilerKind;

    public GccLikeCompilerKind(@NonNls @NotNull String compilerKind_) {
        compilerKind = compilerKind_;
    }

    @Override
    @NonNls
    @NotNull
    public String getKind() {
        return compilerKind;
    }

    @Override
    @NonNls
    @NotNull
    public String getVersionOption() {
        return VERSION_OPTION;
    }

    @Override
    @NonNls
    @NotNull
    public String parseCompilerName(@NonNls @NotNull String versionText) {
        return compilerKind;
    }

    @Override
    @NonNls
    @NotNull
    public String parseCompilerVersion(@NonNls @NotNull String versionText) {
        return parseCompilerVersion(compilerKind, versionText);
    }

    @Override
    @NonNls
    @NotNull
    public String parseCompilerTarget(@NonNls @NotNull String versionText) {
        return versionText.replace('\n', ' ').replaceAll(".*Target: ([^-]*).*", "$1");
    }

    @Override
    @NonNls
    @NotNull
    public List<String> additionalSwitches() {
        return ADDITIONAL_SWITCHES;
    }

    @Override
    @NonNls
    @NotNull
    public List<String> additionalCompilerSwitches(boolean preprocessed) {
        return NO_OPTIONS;
    }

    @Override
    @NonNls
    @NotNull
    public String getLanguageOption(@NotNull Object language) {
        if (language instanceof OCLanguageKind ocLanguageKind) {
            return GCCCompiler.getLanguageOption(ocLanguageKind);
        } else {
            throw new RuntimeException("Unexpected language " + language);
        }
    }

    @NonNls
    @NotNull
    protected String parseCompilerVersion(@NonNls @NotNull String compilerKind, @NonNls @NotNull String versionText) {
        return versionText.replace('\n', ' ').replaceAll(".*" + compilerKind.toLowerCase() + " version ([^ ]*).*", "$1");
    }
}
