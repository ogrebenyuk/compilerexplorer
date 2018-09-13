package com.compilerexplorer.common;

import org.jetbrains.annotations.NotNull;

import java.nio.file.Paths;

public class PathNormalizer {
    @NotNull
    public static String normalizePath(@NotNull String path) {
        String normalizedPath = Paths.get(path).normalize().toString();
        if (isWindowsPath(normalizedPath)) {
            return normalizedPath.toLowerCase();
        }
        return normalizedPath;
    }

    private static boolean isWindowsPath(@NotNull String path) {
        return path.length() >= 2 && path.charAt(1) == ':';
    }
}
