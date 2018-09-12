package com.compilerexplorer.common.datamodel;

import com.compilerexplorer.common.datamodel.state.RemoteCompilerId;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class CompiledText {

    public static class SourceLocation {
        public String file;
        public int line;

        @Override
        public int hashCode() {
            return file.hashCode()
                    + line
                    ;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof SourceLocation)) {
                return false;
            }
            SourceLocation other = (SourceLocation)obj;
            return file.equals(other.file)
                    && line == other.line
                    ;
        }
    }

    public static class CompiledChunk {
        public String text;
        public SourceLocation source;

        @Override
        public int hashCode() {
            return text.hashCode()
                    + source.hashCode()
                    ;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof CompiledChunk)) {
                return false;
            }
            CompiledChunk other = (CompiledChunk)obj;
            return text.equals(other.text)
                    && source.equals(other.source)
                    ;
        }
    }

    public static class CompiledResult {
        public int code;
        public List<CompiledChunk> stdout;
        public List<CompiledChunk> stderr;
        public List<CompiledChunk> asm;

        @Override
        public int hashCode() {
            return code
                    + stdout.hashCode()
                    + stderr.hashCode()
                    + asm.hashCode()
                    ;
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof CompiledResult)) {
                return false;
            }
            CompiledResult other = (CompiledResult)obj;
            return code == other.code
                    && stdout.equals(other.stdout)
                    && stderr.equals(other.stderr)
                    && asm.equals(other.asm)
                    ;
        }
    }

    @NotNull
    private PreprocessedSource preprocessedSource;

    @NotNull
    private RemoteCompilerId compilerId;

    @NotNull
    private CompiledResult compiledResult;

    public CompiledText(@NotNull PreprocessedSource preprocessedSource_, @NotNull RemoteCompilerId compilerId_, @NotNull CompiledResult compiledResult_) {
        preprocessedSource = preprocessedSource_;
        compiledResult = compiledResult_;
        compilerId = compilerId_;
    }

    @NotNull
    public PreprocessedSource getPreprocessedSource() {
        return preprocessedSource;
    }

    @NotNull
    public RemoteCompilerId getCompilerId() {
        return compilerId;
    }

    @NotNull
    public CompiledResult getCompiledResult() {
        return compiledResult;
    }

    @Override
    public int hashCode() {
        return getPreprocessedSource().hashCode()
                + getCompilerId().hashCode()
                + getCompiledResult().hashCode()
                ;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof CompiledText)) {
            return false;
        }
        CompiledText other = (CompiledText)obj;
        return getPreprocessedSource().equals(other.getPreprocessedSource())
                && getCompilerId().equals(other.getCompilerId())
                && getCompiledResult().equals(other.getCompiledResult())
                ;
    }
}
