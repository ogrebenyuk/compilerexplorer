package com.compilerexplorer.common;

import com.jetbrains.cidr.system.HostMachine;
import com.jetbrains.cidr.system.MappedHost;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Paths;
import java.util.List;

public class PathNormalizer {
    @NotNull
    private static final String COMPILER_EXPLORER_DEFAULT_INSTALL_PATH = normalizePath("/opt/compiler-explorer/");

    @NotNull
    public static String normalizePath(@NotNull String path) {
        String normalizedPath = Paths.get(path).normalize().toString();
        if (isWindowsPath(normalizedPath)) {
            return normalizedPath.toLowerCase();
        }
        return normalizedPath;
    }

    @NotNull
    public static String resolvePathFromLocalToCompilerHost(@NotNull String localPath, @NotNull HostMachine host) {
        String normalized = normalizePath(localPath);
        if (host instanceof MappedHost mappedHost) {
            if (mappedHost.getPathMapper().canReplaceLocal(normalized)) {
                return mappedHost.getPathMapper().convertToRemote(normalized);
            }
        }
        return normalized;
    }

    @NotNull
    public static String resolvePathFromCompilerHostToLocal(@NotNull String remotePath, @NotNull HostMachine host, @Nullable String projectBasePath, @Nullable String compilerInstallPath) {
        String normalisedRemotePath = normalizePath(remotePath);
        if (normalisedRemotePath.startsWith(COMPILER_EXPLORER_DEFAULT_INSTALL_PATH)) {
            return remotePath;
        }
        if (!host.isRemote()) {
            return normalisedRemotePath;
        }
        if (host instanceof MappedHost mappedHost) {
            if (mappedHost.getPathMapper().canReplaceRemote(normalisedRemotePath)) {
                return mappedHost.getPathMapper().convertToLocal(normalisedRemotePath);
            }
        }
        if (projectBasePath != null && normalisedRemotePath.startsWith(normalizePath(projectBasePath))) {
            return normalisedRemotePath;
        }
        try {
            String resolved = host.resolveAndCache(List.of(remotePath)).get(0);
            return normalizePath(resolved);
        } catch (Exception e) {
            // empty
        }
        return normalisedRemotePath;
    }

    private static boolean isWindowsPath(@NotNull String path) {
        return path.length() >= 2 && path.charAt(1) == ':';
    }
}
