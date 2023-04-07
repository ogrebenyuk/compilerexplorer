package com.compilerexplorer.common;

import org.jetbrains.annotations.*;

import java.util.ResourceBundle;

public class Bundle {
    @NonNls
    @NotNull
    public static final String BUNDLE_FILE = "messages.compilerexplorerBundle";
    @NotNull
    private static ResourceBundle BUNDLE = ResourceBundle.getBundle(BUNDLE_FILE);

    public static class Substitutor {
        @Nls
        @NotNull
        public static String replace(@Nls @NotNull String format, @NonNls @NotNull String ... map) {
            String result = format;
            for (int i = 0; i + 1 < map.length; i += 2) {
                result = result.replace("${" + map[i] + "}", map[i + 1]);
            }
            if (result.contains("${")) {
                throw new RuntimeException("Bad format " + format);
            }
            return result;
        }
    }

    @Nls
    @NotNull
    public static String get(@NonNls @NotNull @PropertyKey(resourceBundle = BUNDLE_FILE) String key) {
        return BUNDLE.getString(key);
    }

    @Nls
    @NotNull
    public static String format(@NonNls @NotNull @PropertyKey(resourceBundle = BUNDLE_FILE) String key, @NonNls @NotNull String ... map) {
        @Nls @NotNull String result = Substitutor.replace(get(key), map);
        return result;
    }

    @VisibleForTesting
    @NotNull
    public static ResourceBundle adopt(@NotNull ResourceBundle bundle) {
        BUNDLE = bundle;
        return bundle;
    }
}
