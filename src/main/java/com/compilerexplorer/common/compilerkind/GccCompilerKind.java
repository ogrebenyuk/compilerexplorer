package com.compilerexplorer.common.compilerkind;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class GccCompilerKind extends GccLikeCompilerKind {
    @NonNls
    @NotNull
    private static final String GCC_COMPILER_KIND = "GCC";

    public GccCompilerKind() {
        super(GCC_COMPILER_KIND);
    }
}
