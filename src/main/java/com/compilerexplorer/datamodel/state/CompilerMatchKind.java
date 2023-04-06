package com.compilerexplorer.datamodel.state;

import com.compilerexplorer.common.Bundle;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public enum CompilerMatchKind {
    EXACT_MATCH,
    MINOR_MISMATCH,
    NO_MATCH;

    @Nls
    @Nullable
    private static String EXACT_MATCH_TEXT = null;
    @Nls
    @Nullable
    private static String MINOR_MISMATCH_TEXT = null;
    @Nls
    @Nullable
    private static String NO_MATCH_TEXT = null;

    @NotNull
    public static String asString(@NotNull CompilerMatchKind kind) {
        return switch (kind) {
            case EXACT_MATCH -> getExactMatchText();
            case MINOR_MISMATCH -> getMinorMismatchText();
            default -> "";
        };
    }

    @NotNull
    public static String asStringFull(@NotNull CompilerMatchKind kind) {
        return (kind == NO_MATCH) ? getNoMatchText() : asString(kind);
    }

    @Nls
    @NotNull
    private static String getExactMatchText() {
        if (EXACT_MATCH_TEXT == null) {
            EXACT_MATCH_TEXT = Bundle.get("compilerexplorer.CompilerMatchKind.ExactMatch");
        }
        return EXACT_MATCH_TEXT;
    }

    @Nls
    @NotNull
    private static String getMinorMismatchText() {
        if (MINOR_MISMATCH_TEXT == null) {
            MINOR_MISMATCH_TEXT = Bundle.get("compilerexplorer.CompilerMatchKind.MinorVersionMismatch");
        }
        return MINOR_MISMATCH_TEXT;
    }

    @Nls
    @NotNull
    private static String getNoMatchText() {
        if (NO_MATCH_TEXT == null) {
            NO_MATCH_TEXT = Bundle.get("compilerexplorer.CompilerMatchKind.NoMatch");
        }
        return NO_MATCH_TEXT;
    }
}
