package com.compilerexplorer.common.compilerkind;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.Set;

public class CompilerKindFactory {
    @NotNull
    private static final Set<CompilerKind> KNOWN_COMPILER_KINDS = Set.of(
        new GccCompilerKind(),
        new ClangCompilerKind(),
        new NvccCompilerKind()
    );

    @NotNull
    public static Optional<CompilerKind> findCompilerKind(@NonNls @NotNull String compilerKind) {
        return KNOWN_COMPILER_KINDS.stream().filter(kind -> kind.getKind().equals(compilerKind)).findFirst();
    }
}
