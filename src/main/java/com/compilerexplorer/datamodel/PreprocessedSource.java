package com.compilerexplorer.datamodel;

import com.intellij.openapi.util.io.FileUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Arrays;
import java.util.Objects;

public class PreprocessedSource {
    public static final int CODE_GOOD = 0;
    public static final int CODE_REGULAR_BAD = 1;
    public static final int CODE_NOT_PREPROCESSED = -1;

    @NotNull
    public final SourceCompilerSettings sourceCompilerSettings;

    public boolean preprocessLocally;
    @Nullable
    public File compilerWorkingDir;
    @Nullable
    public String[] preprocessorCommandLine;
    public int preprocessorExitCode = CODE_NOT_PREPROCESSED;
    @Nullable
    public String preprocessorStderr;
    @Nullable
    public Exception preprocessorException;
    @Nullable
    public String preprocessedText;

    public PreprocessedSource(@NotNull SourceCompilerSettings sourceCompilerSettings_) {
        sourceCompilerSettings = sourceCompilerSettings_;
    }

    @NotNull
    public SourceCompilerSettings getSourceCompilerSettings() {
        return sourceCompilerSettings;
    }

    public boolean isValid() {
        return !preprocessLocally || preprocessorExitCode == CODE_GOOD;
    }

    @Override
    public int hashCode() {
        return sourceCompilerSettings.hashCode()
                + (preprocessLocally ? 1 : 0)
                + FileUtil.fileHashCode(compilerWorkingDir)
                + (preprocessorCommandLine != null ? Arrays.hashCode(preprocessorCommandLine) : 0)
                + preprocessorExitCode
                + (preprocessorStderr != null ? preprocessorStderr.hashCode() : 0)
                + (preprocessorException != null ? preprocessorException.hashCode() : 0)
                + (preprocessedText != null ? preprocessedText.hashCode() : 0)
                ;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PreprocessedSource other)) {
            return false;
        }
        return sourceCompilerSettings.equals(other.sourceCompilerSettings)
                && preprocessLocally == other.preprocessLocally
                && FileUtil.filesEqual(compilerWorkingDir, other.compilerWorkingDir)
                && Arrays.equals(preprocessorCommandLine, other.preprocessorCommandLine)
                && preprocessorExitCode == other.preprocessorExitCode
                && Objects.equals(preprocessorStderr, other.preprocessorStderr)
                && Objects.equals(preprocessorException, other.preprocessorException)
                && Objects.equals(preprocessedText, other.preprocessedText)
                ;
    }
}
