package com.compilerexplorer.common.component;

import com.intellij.openapi.util.Key;
import org.jetbrains.annotations.NotNull;

public class ResetFlag {
    private static final Key<Boolean> KEY = Key.create(ResetFlag.class.getName());

    public static boolean in(@NotNull DataHolder data) {
        return data.get(KEY).orElse(false);
    }

    public static void add(@NotNull DataHolder data, boolean reset) {
        data.put(KEY, reset);
    }

    public static DataHolder with(@NotNull DataHolder data, boolean reset) {
        return data.with(KEY, reset);
    }

    public static DataHolder without(@NotNull DataHolder data) {
        return data.without(KEY);
    }
}
