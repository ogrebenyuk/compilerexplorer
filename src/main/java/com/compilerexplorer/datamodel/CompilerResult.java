package com.compilerexplorer.datamodel;

import com.intellij.openapi.util.io.FileUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.Arrays;
import java.util.Objects;
import java.util.Optional;

public class CompilerResult {
    public static class Output {
        private final int exitCode;
        @NotNull
        private final String stdout;
        @NotNull
        private final String stderr;
        @Nullable
        private final Exception exception;

        public Output(int exitCode_, @NotNull String stdout_, @NotNull String stderr_, @Nullable Exception exception_) {
            exitCode = exitCode_;
            stdout = stdout_;
            stderr = stderr_;
            exception = exception_;
        }

        public int getExitCode() {
            return exitCode;
        }

        @NotNull
        public String getStdout() {
            return stdout;
        }

        @NotNull
        public String getStderr() {
            return stderr;
        }

        @NotNull
        public Optional<Exception> getException() {
            return Optional.ofNullable(exception);
        }

        @Override
        public int hashCode() {
            return exitCode + stdout.hashCode() + stderr.hashCode() + Objects.hashCode(exception);
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Output other)) {
                return false;
            }
            return exitCode == other.exitCode && stdout.equals(other.stdout) && stderr.equals(other.stderr) && Objects.equals(exception, other.exception);
        }

    }

    @NotNull
    private final File workingDir;
    @NotNull
    private final String[] commandLine;
    @Nullable
    private final Output output;

    public CompilerResult(@NotNull File workingDir_, @NotNull String[] commandLine_, @Nullable Output output_) {
        workingDir = workingDir_;
        commandLine = commandLine_;
        output = output_;
    }

    @NotNull
    public File getWorkingDir() {
        return workingDir;
    }

    @NotNull
    public String[] getCommandLine() {
        return commandLine;
    }

    @NotNull
    public Optional<Output> getOutput() {
        return Optional.ofNullable(output);
    }

    @Override
    public int hashCode() {
        return FileUtil.fileHashCode(workingDir) + Arrays.hashCode(commandLine) + Objects.hashCode(output);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof CompilerResult other)) {
            return false;
        }
        return FileUtil.filesEqual(workingDir, other.workingDir) && Arrays.equals(commandLine, other.commandLine) && Objects.equals(output, other.output);
    }
}
