package com.compilerexplorer.common;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class LanguageUtil {
    @NonNls
    @NotNull
    public static final String CPP_LANG = "C++";
    @NonNls
    @NotNull
    public static final String CIRCLE_LANG = "Circle";
    @NonNls
    @NotNull
    private static final Set<String> CPP_VARIANTS = Set.of(
            CIRCLE_LANG.toLowerCase()
    );


    public static boolean isSourceLanguageCompatibleWithRemote(@NonNls @NotNull String sourceLanguage, @NonNls @NotNull String candidateRemoteLanguage) {
        return candidateRemoteLanguage.equalsIgnoreCase(sourceLanguage)
                || (sourceLanguage.equalsIgnoreCase(CPP_LANG) && isCppVariant(candidateRemoteLanguage));
    }

    private static boolean isCppVariant(@NonNls @NotNull String candidateRemoteLanguage) {
        return CPP_VARIANTS.contains(candidateRemoteLanguage.toLowerCase());
    }
}
