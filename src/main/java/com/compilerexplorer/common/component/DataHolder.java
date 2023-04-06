package com.compilerexplorer.common.component;

import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.UserDataHolderBase;
import com.intellij.util.keyFMap.KeyFMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

public class DataHolder {
    private static class BaseDataHolder extends UserDataHolderBase {
        @NotNull
        public KeyFMap getMap() {
            return getUserMap();
        }
    }

    @NotNull
    private final BaseDataHolder data = new BaseDataHolder();

    public <T> void put(@NotNull Key<T> key, @NotNull T value) {
        data.putUserData(key, value);
    }

    public <T> DataHolder with(@NotNull Key<T> key, @NotNull T value) {
        put(key, value);
        return this;
    }

    public <T> void remove(@NotNull Key<T> key) {
        data.putUserData(key, null);
    }

    public <T> DataHolder without(@NotNull Key<T> key) {
        remove(key);
        return this;
    }

    public <T> Optional<T> get(@NotNull Key<T> key) {
        return Optional.ofNullable(data.getUserData(key));
    }

    public static <T> Optional<T> get(@Nullable DataHolder data, @NotNull Key<T> key) {
        return data != null ? data.get(key) : Optional.empty();
    }

    @NotNull
    public KeyFMap getMap() {
        return data.getMap();
    }
}