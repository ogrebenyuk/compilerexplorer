package com.compilerexplorer.common;

import icons.CidrAsmIcons;
import icons.CidrLangIcons;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
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
    public static final String CUDA_LANG = "CUDA";
    @NonNls
    @NotNull
    public static final String ASM_LANG = "ASM";
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
                || (isC(sourceLanguage) && isCVariant(candidateRemoteLanguage))
                || (isCpp(sourceLanguage) && isCppVariant(candidateRemoteLanguage))
        ;
    }

    private static boolean isC(@NonNls @NotNull String sourceLanguage) {
        return sourceLanguage.equalsIgnoreCase(C_LANG);
    }

    private static boolean isCpp(@NonNls @NotNull String sourceLanguage) {
        return sourceLanguage.equalsIgnoreCase(CPP_LANG);
    }

    private static boolean isCuda(@NonNls @NotNull String sourceLanguage) {
        return sourceLanguage.equalsIgnoreCase(CUDA_LANG);
    }

    private static boolean isAsm(@NonNls @NotNull String sourceLanguage) {
        return sourceLanguage.equalsIgnoreCase(ASM_LANG);
    }

    private static boolean isCVariant(@NonNls @NotNull String candidateRemoteLanguage) {
        return C_VARIANTS.contains(candidateRemoteLanguage.toLowerCase());
    }

    private static boolean isCppVariant(@NonNls @NotNull String candidateRemoteLanguage) {
        return CPP_VARIANTS.contains(candidateRemoteLanguage.toLowerCase());
    }

    @Nullable
    public static Icon getLanguageIcon(@NonNls @NotNull String sourceLanguage) {
        if (isC(sourceLanguage)) {
            return CidrLangIcons.FileTypes.C;
        }
        if (isCpp(sourceLanguage)) {
            return CidrLangIcons.FileTypes.Cpp;
        }
        if (isCuda(sourceLanguage)) {
            return CidrLangIcons.FileTypes.CU;
        }
        if (isAsm(sourceLanguage)) {
            return CidrAsmIcons.Asm;
        }
        return null;
    }
}
