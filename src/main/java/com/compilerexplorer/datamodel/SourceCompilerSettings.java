package com.compilerexplorer.datamodel;

import com.compilerexplorer.datamodel.state.LocalCompilerSettings;
import com.intellij.openapi.util.io.FileUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Arrays;
import java.util.Objects;

public class SourceCompilerSettings {
    public static final int CODE_GOOD = 0;
    public static final int CODE_REGULAR_BAD = 1;
    public static final int CODE_NOT_VERSIONED = -1;

    @NotNull
    public final SourceSettingsConnected sourceSettingsConnected;

    public boolean isSupportedCompilerType;
    @Nullable
    public File versionerWorkingDir;
    @Nullable
    public String[] versionerCommandLine;
    public int versionerExitCode = CODE_NOT_VERSIONED;
    @Nullable
    public String versionerStdout;
    @Nullable
    public String versionerStderr;
    @Nullable
    public Exception versionerException;
    @Nullable
    public LocalCompilerSettings localCompilerSettings;

    public SourceCompilerSettings(@NotNull SourceSettingsConnected sourceSettingsConnected_) {
        sourceSettingsConnected = sourceSettingsConnected_;
    }

    public boolean isValid() {
        return (versionerExitCode == CODE_GOOD || versionerExitCode == CODE_NOT_VERSIONED) && (localCompilerSettings != null);
    }

    public boolean isCached() {
        return versionerCommandLine == null;
    }

    @Override
    public int hashCode() {
        return sourceSettingsConnected.hashCode()
                + (isSupportedCompilerType ? 1 : 0)
                + FileUtil.fileHashCode(versionerWorkingDir)
                + (versionerCommandLine != null ? Arrays.hashCode(versionerCommandLine) : 0)
                + versionerExitCode
                + (versionerStdout != null ? versionerStdout.hashCode() : 0)
                + (versionerStderr != null ? versionerStderr.hashCode() : 0)
                + (versionerException != null ? versionerException.hashCode() : 0)
                + (localCompilerSettings != null ? localCompilerSettings.hashCode() : 0)
                ;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof SourceCompilerSettings other)) {
            return false;
        }
        return sourceSettingsConnected.equals(other.sourceSettingsConnected)
                && isSupportedCompilerType == other.isSupportedCompilerType
                && FileUtil.filesEqual(versionerWorkingDir, other.versionerWorkingDir)
                && Arrays.equals(versionerCommandLine, other.versionerCommandLine)
                && versionerExitCode == other.versionerExitCode
                && Objects.equals(versionerStdout, other.versionerStdout)
                && Objects.equals(versionerStderr, other.versionerStderr)
                && Objects.equals(versionerException, other.versionerException)
                && Objects.equals(localCompilerSettings, other.localCompilerSettings)
                ;
    }
}
