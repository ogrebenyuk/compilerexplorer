package com.compilerexplorer.datamodel;

import com.intellij.openapi.util.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

public class PreprocessedSource {
    public static final Key<PreprocessedSource> KEY = Key.create(PreprocessedSource.class.getName());

    private final boolean preprocessLocally;
    private final boolean canceled;
    @Nullable
    private final CompilerResult result;
    @Nullable
    private final String preprocessedText;

    public PreprocessedSource(boolean preprocessLocally_, boolean canceled_, @Nullable CompilerResult result_, @Nullable String preprocessedText_) {
        preprocessLocally = preprocessLocally_;
        canceled = canceled_;
        result = result_;
        preprocessedText = preprocessedText_;
    }

    public boolean getPreprocessLocally() {
        return preprocessLocally;
    }

    public boolean getCanceled() {
        return canceled;
    }

    @NotNull
    public Optional<CompilerResult> getResult() {
        return Optional.ofNullable(result);
    }

    @NotNull
    public Optional<String> getPreprocessedText() {
        return Optional.ofNullable(preprocessedText);
    }

    @Override
    public int hashCode() {
        return (preprocessLocally ? 1 : 0) + (canceled ? 1 : 0) + Objects.hashCode(result) + Objects.hashCode(preprocessedText);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof PreprocessedSource other)) {
            return false;
        }
        return preprocessLocally == other.preprocessLocally && canceled == other.canceled && Objects.equals(result, other.result) && Objects.equals(preprocessedText, other.preprocessedText);
    }
}
