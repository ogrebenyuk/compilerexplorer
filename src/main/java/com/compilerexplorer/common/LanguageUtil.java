package com.compilerexplorer.common;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class LanguageUtil {
    @NonNls
    @NotNull
    public static final String C_LANG = "C";
    @NonNls
    @NotNull
    public static final String CPP_LANG = "C++";
    @NonNls
    @NotNull
    public static final String CIRCLE_LANG = "Circle";
    @NonNls
    @NotNull
    public static final String C_FOR_OPENCL_LANG = "openclc";
    @NonNls
    @NotNull
    public static final String CPP_FOR_OPENCL_LANG = "cpp_for_opencl";
    @NonNls
    @NotNull
    private static final Set<String> C_VARIANTS = Set.of(
            C_FOR_OPENCL_LANG.toLowerCase()
    );
    @NonNls
    @NotNull
    private static final Set<String> CPP_VARIANTS = Set.of(
            CIRCLE_LANG.toLowerCase(),
            CPP_FOR_OPENCL_LANG.toLowerCase()
    );

    public static boolean isSourceLanguageCompatibleWithRemote(@NonNls @NotNull String sourceLanguage, @NonNls @NotNull String candidateRemoteLanguage) {
        return candidateRemoteLanguage.equalsIgnoreCase(sourceLanguage)
                || (sourceLanguage.equalsIgnoreCase(C_LANG) && isCVariant(candidateRemoteLanguage))
                || (sourceLanguage.equalsIgnoreCase(CPP_LANG) && isCppVariant(candidateRemoteLanguage))
        ;
    }

    private static boolean isCVariant(@NonNls @NotNull String candidateRemoteLanguage) {
        return C_VARIANTS.contains(candidateRemoteLanguage.toLowerCase());
    }

    private static boolean isCppVariant(@NonNls @NotNull String candidateRemoteLanguage) {
        return CPP_VARIANTS.contains(candidateRemoteLanguage.toLowerCase());
    }
}
