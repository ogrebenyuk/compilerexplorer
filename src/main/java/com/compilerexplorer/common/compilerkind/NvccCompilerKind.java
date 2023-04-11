package com.compilerexplorer.common.compilerkind;

import com.compilerexplorer.common.CommandLineUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class NvccCompilerKind implements CompilerKind {
    @NonNls
    @NotNull
    private static final String NO_TARGET = "";
    @NonNls
    @NotNull
    private static final String NVCC_COMPILER_KIND = "NVCC";
    @NonNls
    @NotNull
    private static final String VERSION_OPTION = "--version";
    @NonNls
    @NotNull
    private static final String LANGUAGE_OPTION = "-x cu";
    @NonNls
    @NotNull
    private static final String DRY_RUN_OPTION = "-dryrun";
    @NonNls
    @NotNull
    private static final String STDIN_TMP_FILE_SUFFIX = "_stdin";
    @NonNls
    @NotNull
    private static final String DEFINE_OPTION = "-D";
    @NonNls
    @NotNull
    private static final String CUDA_RUNTIME_INCLUDE_GUARD = "__CUDA_RUNTIME_H__";

    @Override
    @NonNls
    @NotNull
    public String getKind() {
        return NVCC_COMPILER_KIND;
    }

    @Override
    @NonNls
    @NotNull
    public String getVersionOption() {
        return VERSION_OPTION;
    }

    @Override
    @NonNls
    @NotNull
    public String parseCompilerVersion(@NonNls @NotNull String versionText) {
        return versionText.replace('\n', ' ').replaceAll(".*, release [^,]*, V([^ ]*).*", "$1");
    }

    @Override
    @NonNls
    @NotNull
    public String parseCompilerTarget(@NonNls @NotNull String versionText) {
        return NO_TARGET;
    }

    @Override
    @NonNls
    @NotNull
    public List<String> additionalSwitches() {
        return NO_OPTIONS;
    }

    @Override
    @NonNls
    @NotNull
    public List<String> additionalCompilerSwitches(boolean preprocessed) {
        if (preprocessed) {
            return List.of(DEFINE_OPTION, CUDA_RUNTIME_INCLUDE_GUARD);
        } else {
            return NO_OPTIONS;
        }
    }

    @Override
    @NonNls
    @NotNull
    public String getLanguageOption(@NotNull Object language) {
        return LANGUAGE_OPTION;
    }

    @NonNls
    @NotNull
    public List<String> getPreprocessOptions() {
        List<String> result = new ArrayList<>(DEFAULT_PREPROCESS_OPTIONS);
        result.add(DRY_RUN_OPTION);
        return result;
    }

    @Override
    public boolean twoPassPreprocessor() {
        return true;
    }

    @Override
    @NonNls
    @NotNull
    public List<String> getSecondPassPreprocessOptions(@NotNull String firstPassStderr) {
        String[] lines = firstPassStderr.split("\n");
        if (lines.length > 0) {
            String command = lines[lines.length - 1];
            command = command.replaceAll("^[^ ]* ", "");
            if (!command.isEmpty()) {
                List<String> options = CommandLineUtil.parseCommandLine(command);
                return options.stream()
                        .map(option -> option.endsWith(STDIN_TMP_FILE_SUFFIX) ? DEFAULT_STDIN_FILE_MARKER : option)
                        .toList();
            }
        }
        return List.of();
    }
}
