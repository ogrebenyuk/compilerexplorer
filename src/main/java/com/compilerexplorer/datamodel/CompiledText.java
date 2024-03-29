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

         @Override
        public int hashCode() {
            return Objects.hashCode(file)
                    + line
                    ;
        }

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
        @Nullable
        public String text;
        @Nullable
        public SourceLocation source;
        @Nullable
        public List<String> opcodes;
        public Integer address;

        @Override
        public int hashCode() {
            return Objects.hashCode(text)
                    + Objects.hashCode(source)
                    + Objects.hashCode(opcodes)
                    + Objects.hashCode(address)
                    ;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof CompiledChunk other)) {
                return false;
            }
            return Objects.equals(text, other.text)
                    && Objects.equals(source, other.source)
                    && Objects.equals(opcodes, other.opcodes)
                    && Objects.equals(address, other.address)
                    ;
        }
    }

    public static class ProcessOutput {
        public int code;
        @Nullable
        public List<CompiledChunk> stdout;
        @Nullable
        public List<CompiledChunk> stderr;

        @Override
        public int hashCode() {
            return code
                    + Objects.hashCode(stdout)
                    + Objects.hashCode(stderr)
                    ;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof ProcessOutput other)) {
                return false;
            }
            return code == other.code
                    && Objects.equals(stdout, other.stdout)
                    && Objects.equals(stderr, other.stderr)
                    ;
        }
    }

    public static class ExecResult extends ProcessOutput {
        public boolean didExecute;
        @Nullable
        public ProcessOutput buildResult;

        @Override
        public int hashCode() {
            return super.hashCode()
                    + (didExecute ? 1 : 0)
                    + Objects.hashCode(buildResult)
                    ;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof ExecResult other)) {
                return false;
            }
            return super.equals(obj)
                    && didExecute == other.didExecute
                    && Objects.equals(buildResult, other.buildResult)
                    ;
        }
    }

    public static class AsmResult {
        @Nullable
        public List<CompiledChunk> asm;
        @Nullable
        public Map<String, Integer> labelDefinitions;

        @Override
        public int hashCode() {
            return Objects.hashCode(asm) + Objects.hashCode(labelDefinitions);
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof AsmResult other)) {
                return false;
            }
            return Objects.equals(asm, other.asm) && Objects.equals(labelDefinitions, other.labelDefinitions);
        }
    }

    public static class CompiledResult extends AsmResult {
        public static final int CODE_GOOD = 0;
        public static final int CODE_NOT_COMPILED = -1;

        @Nullable
        public String inputFilename;
        public int code = CODE_NOT_COMPILED;
        @Nullable
        public List<CompiledChunk> stdout;
        @Nullable
        public List<CompiledChunk> stderr;
        @Nullable
        public ExecResult execResult;
        @Nullable
        public Map<String, AsmResult> devices;

        @Override
        public int hashCode() {
            return super.hashCode()
                    + Objects.hashCode(inputFilename)
                    + code
                    + Objects.hashCode(stdout)
                    + Objects.hashCode(stderr)
                    + Objects.hashCode(execResult)
                    + Objects.hashCode(devices)
                    ;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof CompiledResult other)) {
                return false;
            }
            return super.equals(obj)
                    && Objects.equals(inputFilename, other.inputFilename)
                    && code == other.code
                    && Objects.equals(stdout, other.stdout)
                    && Objects.equals(stderr, other.stderr)
                    && Objects.equals(execResult, other.execResult)
                    && Objects.equals(devices, other.devices)
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
        return compiledResult != null && compiledResult.code == CompiledResult.CODE_GOOD && exception == null ? Optional.of(compiledResult) : Optional.empty();
    }

    @NotNull
    public Optional<ExecResult> getExecResult() {
        return compiledResult != null ? Optional.ofNullable(compiledResult.execResult) : Optional.empty();
    }

    @Override
    public int hashCode() {
        return (canceled ? 1 : 0)
                + rawInput.hashCode()
                + rawOutput.hashCode()
                + Objects.hashCode(exception)
                + Objects.hashCode(compiledResult)
                ;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof CompiledText other)) {
            return false;
        }
        return canceled == other.canceled
                && rawInput.equals(other.rawInput)
                && rawOutput.equals(other.rawOutput)
                && Objects.equals(exception, other.exception)
                && Objects.equals(compiledResult, other.compiledResult)
                ;
    }
}
