package com.compilerexplorer.datamodel.state;

import com.compilerexplorer.common.Bundle;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;

public enum CompilerMatchKind {
    EXACT_MATCH,
    MINOR_MISMATCH,
    NO_MATCH;

    @Nls
    @NotNull
    private static final String EXACT_MATCH_TEXT = Bundle.get("compilerexplorer.CompilerMatchKind.ExactMatch");
    @Nls
    @NotNull
    private static final String MINOR_MISMATCH_TEXT = Bundle.get("compilerexplorer.CompilerMatchKind.MinorVersionMismatch");
    @Nls
    @NotNull
    private static final String NO_MATCH_TEXT = Bundle.get("compilerexplorer.CompilerMatchKind.NoMatch");

    @NotNull
    public static String asString(@NotNull CompilerMatchKind kind) {
        return switch (kind) {
            case EXACT_MATCH -> EXACT_MATCH_TEXT;
            case MINOR_MISMATCH -> MINOR_MISMATCH_TEXT;
            default -> "";
        };
    }

    @NotNull
    public static String asStringFull(@NotNull CompilerMatchKind kind) {
        return (kind == NO_MATCH) ? NO_MATCH_TEXT : asString(kind);
    }
}
