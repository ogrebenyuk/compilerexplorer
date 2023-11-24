package com.compilerexplorer.common.compilerkind;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ClangCompilerKind extends ClangBasedCompilerKind {
    @NonNls
    @NotNull
    private static final String CLANG_COMPILER_KIND = "Clang";
    @NonNls
    @NotNull
    private static final List<String> CLANG_COMPILER_FILENAMES = ImmutableList.of("clang", "clang++", "icx", "icx-cc", "icx-cl", "icpx");

    public ClangCompilerKind() {
        super(CLANG_COMPILER_KIND, CLANG_COMPILER_FILENAMES);
    }
}
