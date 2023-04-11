package com.compilerexplorer.common.compilerkind;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.Collections;
import java.util.List;

public interface CompilerKind {
    @NonNls
    @NotNull
    String DEFAULT_INCLUDE_OPTION = "-I";
    @NonNls
    @NotNull
    String DEFAULT_STDIN_FILE_MARKER = "-";
    @NonNls
    @NotNull
    String DEFAULT_STDOUT_FILE_MARKER = "-";
    @NonNls
    @NotNull
    List<String> DEFAULT_PREPROCESS_OPTIONS = List.of("-o", DEFAULT_STDOUT_FILE_MARKER, "-E", DEFAULT_STDIN_FILE_MARKER);
    @NonNls
    @NotNull
    List<String> NO_OPTIONS = Collections.emptyList();

    @NonNls
    @NotNull
    String getKind();

    @NonNls
    @NotNull
    String getVersionOption();

    @NonNls
    @NotNull
    String parseCompilerVersion(@NonNls @NotNull String versionText);

    @NonNls
    @NotNull
    String parseCompilerTarget(@NonNls @NotNull String versionText);

    @NonNls
    @NotNull
    List<String> additionalSwitches();

    @NonNls
    @NotNull
    List<String> additionalCompilerSwitches(boolean preprocessed);

    @NonNls
    @NotNull
    String getLanguageOption(@NotNull Object language);

    @SuppressWarnings("SameReturnValue")
    @NonNls
    @NotNull
    default String getIncludeOption() {
        return DEFAULT_INCLUDE_OPTION;
    }

    @NonNls
    @NotNull
    default List<String> getPreprocessOptions() {
        return DEFAULT_PREPROCESS_OPTIONS;
    }

    default boolean twoPassPreprocessor() {
        return false;
    }

    @NonNls
    @NotNull
    default List<String> getSecondPassPreprocessOptions(@NotNull String firstPassStderr) {
        return DEFAULT_PREPROCESS_OPTIONS;
    }
}