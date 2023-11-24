package com.compilerexplorer.common.compilerkind;

import com.google.common.collect.ImmutableList;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class GccCompilerKind extends GccLikeCompilerKind {
    @NonNls
    @NotNull
    private static final String GCC_COMPILER_KIND = "GCC";
    @NonNls
    @NotNull
    private static final List<String> GCC_COMPILER_FILENAMES = ImmutableList.of("gcc", "g++");

    public GccCompilerKind() {
        super(GCC_COMPILER_KIND, GCC_COMPILER_FILENAMES);
    }
}
