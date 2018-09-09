package com.compilerexplorer.common.datamodel.state;

import org.jetbrains.annotations.NotNull;

public enum CompilerMatchKind {
    EXACT_MATCH,
    MINOR_MISMATCH,
    NO_MATCH;

    @NotNull
    public static String asString(@NotNull CompilerMatchKind kind) {
        switch (kind) {
            case EXACT_MATCH: return "exact match";
            case MINOR_MISMATCH: return "minor version mismatch";
        }
        return "";
    }
}
