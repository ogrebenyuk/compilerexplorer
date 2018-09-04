package com.compilerexplorer.common;

import org.jetbrains.annotations.NotNull;

public class PreprocessedSource {
    @NotNull
    private final SourceSettings sourceSettings;
    @NotNull
    private final String preprocessedText;
    @NotNull
    private final String language;
    @NotNull
    private final String compilerName;
    @NotNull
    private final String compilerVersion;
    @NotNull
    private final String compilerTarget;

    public PreprocessedSource(@NotNull SourceSettings sourceSettings_,
                              @NotNull String preprocessedText_,
                              @NotNull String language_,
                              @NotNull String compilerName_,
                              @NotNull String compilerVersion_,
                              @NotNull String compilerTarget_) {
        sourceSettings = sourceSettings_;
        preprocessedText = preprocessedText_;
        language = language_;
        compilerVersion = compilerVersion_;
        compilerName = compilerName_;
        compilerTarget = compilerTarget_;
    }

    @NotNull
    public SourceSettings getSourceSettings() {
        return sourceSettings;
    }

    @NotNull
    public String getPreprocessedText() {
        return preprocessedText;
    }

    @NotNull
    public String getLanguage() {
        return language;
    }

    @NotNull
    public String getCompilerName() {
        return compilerName;
    }

    @NotNull
    public String getCompilerVersion() {
        return compilerVersion;
    }

    @NotNull
    public String getCompilerTarget() {
        return compilerTarget;
    }
}
