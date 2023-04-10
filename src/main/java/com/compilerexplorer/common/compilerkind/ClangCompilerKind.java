package com.compilerexplorer.common.compilerkind;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class ClangCompilerKind extends GccLikeCompilerKind {
    @NonNls
    @NotNull
    private static final String CLANG_COMPILER_KIND = "Clang";

    public ClangCompilerKind() {
        super(CLANG_COMPILER_KIND);
    }
}
