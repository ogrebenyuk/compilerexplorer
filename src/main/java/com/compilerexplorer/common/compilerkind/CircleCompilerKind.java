package com.compilerexplorer.common.compilerkind;

import com.compilerexplorer.common.LanguageUtil;
import com.jetbrains.cidr.lang.OCLanguageKind;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

public class CircleCompilerKind extends GccLikeCompilerKind {
    @NonNls
    @NotNull
    private static final String CIRCLE_COMPILER_KIND = "Circle";
    @NonNls
    @NotNull
    private static final Set<String> UNRECOGNIZED_OPTIONS = Set.of(
            "-fdiagnostics-color"
    );

    public CircleCompilerKind() {
        super(CIRCLE_COMPILER_KIND);
    }

    @Override
    @NonNls
    @NotNull
    public List<String> additionalSwitches() {
        return NO_OPTIONS;
    }

    @Override
    @NonNls
    @NotNull
    public String getLanguageOption(@NotNull Object language) {
        if (language instanceof OCLanguageKind ocLanguageKind && ocLanguageKind.isCpp()) {
            return "";
        } else {
            throw new RuntimeException("Unexpected language " + language);
        }
    }

    @Override
    @NotNull
    public List<String> adjustSourceSwitches(@NotNull List<String> sourceSwitches) {
        return sourceSwitches.stream()
                .filter(s -> UNRECOGNIZED_OPTIONS.stream().noneMatch(s::startsWith))
                .toList();
    }

    @Override
    @NonNls
    @NotNull
    public String adjustSourceLanguage(@NonNls @NotNull String sourceLanguage) {
        if (sourceLanguage.equalsIgnoreCase(LanguageUtil.CPP_LANG)) {
            return LanguageUtil.CIRCLE_LANG;
        } else {
            throw new RuntimeException("Unexpected source language " + sourceLanguage);
        }
    }

    @Override
    public boolean allowSourceFilenameMarker() {
        return false;
    }
}
