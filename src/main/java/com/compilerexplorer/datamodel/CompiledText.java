package com.compilerexplorer.datamodel;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
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

        public String text;
        public SourceLocation source;
        @Nullable
        public List<String> opcodes;
        public int address = NO_ADDRESS;

        @SuppressWarnings("unused")
        @Override
        public int hashCode() {
            return text.hashCode()
                    + source.hashCode()
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
            return text.equals(other.text)
                    && source.equals(other.source)
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
        public int code;
        public List<CompiledChunk> stdout;
        public List<CompiledChunk> stderr;
        public List<CompiledChunk> asm;
        @Nullable
        public ExecResult execResult;

        @SuppressWarnings("WeakerAccess")
        @Override
        public int hashCode() {
            return code
                    + stdout.hashCode()
                    + stderr.hashCode()
                    + asm.hashCode()
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
                    && stdout.equals(other.stdout)
                    && stderr.equals(other.stderr)
                    && asm.equals(other.asm)
                    && Objects.equals(execResult, other.execResult)
                    ;
        }
    }

    @NotNull
    private final PreprocessedSource preprocessedSource;

    @NotNull
    private final CompiledResult compiledResult;

    public CompiledText(@NotNull PreprocessedSource preprocessedSource_, @NotNull CompiledResult compiledResult_) {
        preprocessedSource = preprocessedSource_;
        compiledResult = compiledResult_;
    }

    @NotNull
    public PreprocessedSource getPreprocessedSource() {
        return preprocessedSource;
    }

    @NotNull
    public CompiledResult getCompiledResult() {
        return compiledResult;
    }

    @SuppressWarnings("unused")
    @Override
    public int hashCode() {
        return getPreprocessedSource().hashCode()
                + getCompiledResult().hashCode()
                ;
    }

    @SuppressWarnings("unused")
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof CompiledText other)) {
            return false;
        }
        return getPreprocessedSource().equals(other.getPreprocessedSource())
                && getCompiledResult().equals(other.getCompiledResult())
                ;
    }
}
