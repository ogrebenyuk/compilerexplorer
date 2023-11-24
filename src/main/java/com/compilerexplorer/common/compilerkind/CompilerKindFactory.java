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
        new NvccCompilerKind(),
        new CircleCompilerKind()
    );

    @NotNull
    public static Optional<CompilerKind> findCompilerKind(@NonNls @NotNull String compilerKind) {
        return KNOWN_COMPILER_KINDS.stream().filter(kind -> kind.getKind().equalsIgnoreCase(compilerKind)).findFirst();
    }

    @NotNull
    public static Optional<CompilerKind> findCompilerKindFromExecutableFilename(@NonNls @NotNull String executableFilename) {
        return KNOWN_COMPILER_KINDS.stream().filter(kind -> kind.getExecutableFilenames().contains(executableFilename)).findFirst();
    }
}
