package com.compilerexplorer.datamodel;

import com.intellij.openapi.util.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

public class CompiledText {
    public static final Key<CompiledText> KEY = Key.create(CompiledText.class.getName());

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

    private final boolean canceled;
    @NotNull
    private final String rawInput;
    @NotNull
    private final String rawOutput;
    @Nullable
    private final Exception exception;
    @Nullable
    private final CompiledResult compiledResult;

    public CompiledText(boolean canceled_, @NotNull String rawInput_, @NotNull String rawOutput_, @Nullable Exception exception_, @Nullable CompiledResult compiledResult_) {
        canceled = canceled_;
        rawInput = rawInput_;
        rawOutput = rawOutput_;
        exception = exception_;
        compiledResult = compiledResult_;
    }

    public boolean getCanceled() {
        return canceled;
    }

    @NotNull
    public String getRawInput() {
        return rawInput;
    }

    @NotNull
    public String getRawOutput() {
        return rawOutput;
    }

    @NotNull
    public Optional<Exception> getException() {
        return Optional.ofNullable(exception);
    }

    @NotNull
    public Optional<CompiledResult> getCompiledResult() {
        return Optional.ofNullable(compiledResult);
    }

    @NotNull
    public Optional<CompiledResult> getCompiledResultIfGood() {
        return compiledResult != null && compiledResult.code == CompiledResult.CODE_GOOD ? Optional.of(compiledResult) : Optional.empty();
    }

    @NotNull
    public Optional<ExecResult> getExecResult() {
        return compiledResult != null ? Optional.ofNullable(compiledResult.execResult) : Optional.empty();
    }

    @Override
    public int hashCode() {
        return (canceled ? 1 : 0) + rawInput.hashCode() + rawOutput.hashCode() + Objects.hashCode(exception) + Objects.hashCode(compiledResult);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof CompiledText other)) {
            return false;
        }
        return canceled == other.canceled && rawInput.equals(other.rawInput) && rawOutput.equals(other.rawOutput) && Objects.equals(exception, other.exception) && Objects.equals(compiledResult, other.compiledResult);
    }
}
