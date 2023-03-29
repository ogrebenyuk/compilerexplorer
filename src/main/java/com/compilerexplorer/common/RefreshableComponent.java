package com.compilerexplorer.common;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class RefreshableComponent<T> implements Consumer<T>, Refreshable {
    @Nullable
    protected T lastT;

    @Override
    public void accept(@NotNull T t) {
        lastT = t;
    }

    @Override
    public void refresh() {
        if (lastT != null) {
            accept(lastT);
        }
    }
}
