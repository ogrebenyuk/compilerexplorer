package com.compilerexplorer.datamodel;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;

public class CompiledText {

    public static class SourceLocation {
        @Nullable
        public String file;
        public int line;

        public SourceLocation(@NotNull SourceLocation other) {
            file = other.file;
            line = other.line;
        }

        public SourceLocation(@NotNull String file_, int line_) {
            file = file_;
            line = line_;
        }

        @Override
        @NotNull
        public String toString()
        {
            return (file != null ? file : "") + ":" + line;
        }

        @SuppressWarnings("WeakerAccess")
        @Override
        public int hashCode() {
            return (file != null ? file.hashCode() : 0)
                    + line
                    ;
        }

        @SuppressWarnings("WeakerAccess")
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof SourceLocation other)) {
                return false;
            }
            return Objects.equals(file, other.file)
                    && line == other.line
                    ;
        }
    }

    public static class CompiledChunk {
        public final static int NO_ADDRESS = -1;

        @Nullable
        public String text;
        @Nullable
        public SourceLocation source;
        @Nullable
        public List<String> opcodes;
        public int address = NO_ADDRESS;

        @SuppressWarnings("unused")
        @Override
        public int hashCode() {
            return (text != null ? text.hashCode() : 0)
                    + (source != null ? source.hashCode() : 0)
                    + (opcodes != null ? opcodes.hashCode() : 0)
                    + address
                    ;
        }

        @SuppressWarnings("unused")
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof CompiledChunk other)) {
                return false;
            }
            return Objects.equals(text, other.text)
                    && Objects.equals(source, other.source)
                    && Objects.equals(opcodes, other.opcodes)
                    && address == other.address
                    ;
        }
    }

    public static class ExecResult {
        public List<CompiledChunk> stdout;

        @Override
        public int hashCode() {
            return stdout.hashCode()
                    ;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof ExecResult other)) {
                return false;
            }
            return stdout.equals(other.stdout)
                    ;
        }
    }

    public static class CompiledResult {
        public static final int CODE_GOOD = 0;
        public static final int CODE_REGULAR_BAD = 1;
        public static final int CODE_NOT_COMPILED = -1;

        public int code = CODE_NOT_COMPILED;
        @Nullable
        public List<CompiledChunk> stdout;
        @Nullable
        public List<CompiledChunk> stderr;
        @Nullable
        public List<CompiledChunk> asm;
        @Nullable
        public Map<String, Integer> labelDefinitions;
        @Nullable
        public ExecResult execResult;

        public boolean isValid() {
            return code == CODE_GOOD;
        }

        @SuppressWarnings("WeakerAccess")
        @Override
        public int hashCode() {
            return code
                    + (stdout != null ? stdout.hashCode() : 0)
                    + (stderr != null ? stderr.hashCode() : 0)
                    + (asm != null ? asm.hashCode() : 0)
                    + (labelDefinitions != null ? labelDefinitions.hashCode() : 0)
                    + (execResult != null ? execResult.hashCode() : 0)
                    ;
        }

        @SuppressWarnings("WeakerAccess")
        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof CompiledResult other)) {
                return false;
            }
            return code == other.code
                    && Objects.equals(stdout, other.stdout)
                    && Objects.equals(stderr, other.stderr)
                    && Objects.equals(asm, other.asm)
                    && Objects.equals(labelDefinitions, other.labelDefinitions)
                    && Objects.equals(execResult, other.execResult)
                    ;
        }
    }

    @NotNull
    public final SourceRemoteMatched sourceRemoteMatched;

    @Nullable
    public String rawInput;
    @Nullable
    public String rawOutput;
    @Nullable
    public Exception exception;
    @Nullable
    public CompiledResult compiledResult;

    public CompiledText(@NotNull SourceRemoteMatched sourceRemoteMatched_) {
        sourceRemoteMatched = sourceRemoteMatched_;
    }

    public boolean isValid() {
        return compiledResult != null && compiledResult.isValid();
    }

    public boolean isValidExecResult() {
        return compiledResult != null && compiledResult.isValid() && compiledResult.execResult != null;
    }

    @SuppressWarnings("unused")
    @Override
    public int hashCode() {
        return sourceRemoteMatched.hashCode()
                + (rawInput != null ? rawInput.hashCode() : 0)
                + (rawOutput != null ? rawOutput.hashCode() : 0)
                + (exception != null ? exception.hashCode() : 0)
                + (compiledResult != null ? compiledResult.hashCode() : 0)
                ;
    }

    @SuppressWarnings("unused")
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof CompiledText other)) {
            return false;
        }
        return sourceRemoteMatched.equals(other.sourceRemoteMatched)
                && Objects.equals(rawInput, other.rawInput)
                && Objects.equals(rawOutput, other.rawOutput)
                && Objects.equals(exception, other.exception)
                && Objects.equals(compiledResult, other.compiledResult)
                ;
    }
}
