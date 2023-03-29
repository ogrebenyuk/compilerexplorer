package com.compilerexplorer.common;

import org.jetbrains.annotations.NotNull;

public enum RefreshSignal {
    RESET(0),
    RECONNECT(1),
    PREPROCESS(2),
    COMPILE(3);

    public final int value;

    RefreshSignal(int value_) {
        value = value_;
    }

    public boolean strongerThan(@NotNull RefreshSignal other) {
        return value < other.value;
    }
}
