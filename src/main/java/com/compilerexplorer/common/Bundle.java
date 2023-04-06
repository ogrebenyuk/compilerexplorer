package com.compilerexplorer.common;

import org.apache.commons.text.StringSubstitutor;
import org.apache.commons.text.lookup.StringLookup;
import org.jetbrains.annotations.*;

import java.util.ResourceBundle;

public class Bundle {
    @NonNls
    @NotNull
    public static final String BUNDLE_FILE = "messages.compilerexplorerBundle";
    @NotNull
    private static ResourceBundle BUNDLE = ResourceBundle.getBundle(BUNDLE_FILE);
    private static class ThrowingLookup implements StringLookup {
        public String[] map = null;
        @Override
        @Nls
        @NotNull
        public String lookup(@NonNls @NotNull String key) {
            if (map != null) {
                for (int i = 0; i + 1 < map.length; i += 2) {
                    if (map[i].equals(key)) {
                        return map[i + 1];
                    }
                }
                throw new RuntimeException("Cannot find " + key + " among " + String.join(" ", map));
            }
            throw new RuntimeException("Cannot find " + key);
        }
    }
    @NotNull
    private static final ThrowingLookup LOOKUP = new ThrowingLookup();
    @NotNull
    private static final StringSubstitutor SUBSTITUTOR = new StringSubstitutor(LOOKUP);

    @Nls
    @NotNull
    public static String get(@NonNls @NotNull @PropertyKey(resourceBundle = BUNDLE_FILE) String key) {
        return BUNDLE.getString(key);
    }

    @Nls
    @NotNull
    public static String format(@NonNls @NotNull @PropertyKey(resourceBundle = BUNDLE_FILE) String key, @NonNls @NotNull String ... map) {
        LOOKUP.map = map;
        @Nls @NotNull String result = SUBSTITUTOR.replace(get(key));
        LOOKUP.map = null;
        return result;
    }

    @VisibleForTesting
    @NotNull
    public static ResourceBundle adopt(@NotNull ResourceBundle bundle) {
        BUNDLE = bundle;
        return bundle;
    }
}
