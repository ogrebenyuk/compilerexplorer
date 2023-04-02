package com.compilerexplorer.common.component;

import com.intellij.openapi.util.Key;
import org.jetbrains.annotations.NotNull;

public enum ResetLevel {
    NONE,
    RESET,
    RECONNECT,
    PREPROCESS,
    COMPILE;

    public static final Key<ResetLevel> KEY = Key.create(ResetLevel.class.getName());

    @NotNull
    public static DataHolder with(@NotNull DataHolder data, ResetLevel level) {
        return data.with(KEY, level);
    }
}
