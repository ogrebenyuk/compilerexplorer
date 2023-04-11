package com.compilerexplorer.gui.tabs;

import com.compilerexplorer.common.Bundle;
import com.compilerexplorer.common.CommandLineUtil;
import com.compilerexplorer.common.Tabs;
import com.compilerexplorer.datamodel.CompilerResult;
import com.compilerexplorer.datamodel.state.SettingsState;
import com.intellij.openapi.fileTypes.FileType;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public abstract class BasePreprocessorUtilProvider extends BaseTabProvider {
    public BasePreprocessorUtilProvider(@NotNull SettingsState state, @NotNull Tabs tab, @NonNls @NotNull String actionId, @NotNull FileType fileType) {
        super(state, tab, actionId, fileType);
    }

    @Nls
    @NotNull
    protected static String getPreprocessorErrorMessage(@Nullable CompilerResult result, @NotNull CompilerResult.Output output) {
        StringBuilder errorMessageBuilder = new StringBuilder();
        if (result != null) {
            errorMessageBuilder.append(Bundle.format("compilerexplorer.BasePreprocessorUtilProvider.CommandLine", "CommandLine", getCommandLine(result), "WorkingDir", result.getWorkingDir()));
            errorMessageBuilder.append("\n");
        }
        output.getException().ifPresentOrElse(
                exception -> {
                    errorMessageBuilder.append(Bundle.format("compilerexplorer.BasePreprocessorUtilProvider.Exception", "Exception", exception.getMessage()));
                    errorMessageBuilder.append("\n");
                },
                () -> {
                    if (output.getExitCode() != 0) {
                        errorMessageBuilder.append(Bundle.format("compilerexplorer.BasePreprocessorUtilProvider.ExitCode", "Code", Integer.toString(output.getExitCode())));
                        errorMessageBuilder.append("\n");
                    }
                    errorMessageBuilder.append(output.getStderr());
                }
        );
        return errorMessageBuilder.toString();
    }

    @NonNls
    @NotNull
    private static String getCommandLine(@NotNull CompilerResult result) {
        return CommandLineUtil.formCommandLine(Arrays.stream(result.getCommandLine()).toList());
    }
}
