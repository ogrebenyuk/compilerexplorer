package com.compilerexplorer.common.datamodel;

import com.compilerexplorer.common.datamodel.state.RemoteCompilerId;
import com.intellij.openapi.util.io.FileUtil;
import org.jetbrains.annotations.NotNull;

public class CompiledText {
    @NotNull
    private PreprocessedSource preprocessedSource;

    @NotNull
    private RemoteCompilerId compilerId;

    @NotNull
    private String compiledText;

    public CompiledText(@NotNull PreprocessedSource preprocessedSource_, @NotNull RemoteCompilerId compilerId_, @NotNull String compiledText_) {
        preprocessedSource = preprocessedSource_;
        compiledText = compiledText_;
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
    public String getCompiledText() {
        return compiledText;
    }

    @Override
    public int hashCode() {
        return getPreprocessedSource().hashCode()
                + getCompilerId().hashCode()
                + getCompiledText().hashCode()
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
                && getCompiledText().equals(other.getCompiledText())
                ;
    }
}
