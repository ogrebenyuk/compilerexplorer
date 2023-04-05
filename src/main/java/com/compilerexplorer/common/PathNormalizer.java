package com.compilerexplorer.common;

import com.intellij.openapi.util.SystemInfo;
import com.jetbrains.cidr.system.HostMachine;
import com.jetbrains.cidr.system.MappedHost;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.nio.file.Paths;
import java.util.List;

public class PathNormalizer {
    @NonNls
    @NotNull
    private static final String COMPILER_EXPLORER_DEFAULT_INSTALL_PATH = normalizePath("/opt/compiler-explorer/");

    @NonNls
    @NotNull
    public static String normalizePath(@NonNls @NotNull String path) {
        String normalizedPath = Paths.get(path).normalize().toString();
        if (isWindowsPath(normalizedPath)) {
            return normalizedPath.toLowerCase();
        }
        return normalizedPath;
    }

    @NonNls
    @NotNull
    public static String resolvePathFromLocalToCompilerHost(@NonNls @NotNull String localPath, @NotNull HostMachine host) {
        String normalized = normalizePath(localPath);
        if (host instanceof MappedHost mappedHost) {
            if (mappedHost.getPathMapper().canReplaceLocal(normalized)) {
                return mappedHost.getPathMapper().convertToRemote(normalized);
            }
        }
        return normalized;
    }

    @NonNls
    @NotNull
    public static String resolvePathFromCompilerHostToLocal(@NonNls @NotNull String remotePath, @NotNull HostMachine host, @NonNls @Nullable String projectBasePath, @NonNls @Nullable String compilerInstallPath) {
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
        if (projectBasePath != null) {
            String normalizedProjectPath = normalizePath(projectBasePath);
            if (normalisedRemotePath.startsWith(normalizedProjectPath)) {
                return normalisedRemotePath;
            }
            if (SystemInfo.isWindows && isWindowsPath(normalizedProjectPath) && !isWindowsPath(normalisedRemotePath)) {
                String fixed = normalizedProjectPath.substring(0, 2) + normalisedRemotePath;
                if (fixed.startsWith(normalizedProjectPath)) {
                    return fixed;
                }
            }
        }
        try {
            String resolved = host.resolveAndCache(List.of(remotePath)).get(0);
            return normalizePath(resolved);
        } catch (Exception e) {
            // empty
        }
        return normalisedRemotePath;
    }

    private static boolean isWindowsPath(@NonNls @NotNull String path) {
        return path.length() >= 2 && path.charAt(1) == ':';
    }
}
