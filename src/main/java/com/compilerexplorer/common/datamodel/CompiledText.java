package com.compilerexplorer.common.datamodel;

import com.compilerexplorer.common.datamodel.state.RemoteCompilerId;
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
}
