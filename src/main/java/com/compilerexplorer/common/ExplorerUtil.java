package com.compilerexplorer.common;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class ExplorerUtil {
    @NonNls
    @NotNull
    public static final String ACCEPT_HEADER = "accept";
    @NonNls
    @NotNull
    public static final String JSON_MIME_TYPE = "application/json";
    @NonNls
    @NotNull
    public static final String COMPILERS_ENDPOINT = "/api/compilers";
    @NonNls
    @NotNull
    public static final String COMPILER_API_ROOT = "/api/compiler/";
    @NonNls
    @NotNull
    public static final String COMPILE_ENDPOINT = "/compile";
    @NonNls
    @NotNull
    public static final String LIBRARIES_ENDPOINT = "/api/libraries/";

    @NonNls
    @NotNull
    public static String language(@NonNls @NotNull String compilerLanguage) {
        return compilerLanguage.toLowerCase();
    }
}
