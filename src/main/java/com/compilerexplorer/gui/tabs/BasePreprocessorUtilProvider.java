package com.compilerexplorer.gui.tabs;

import com.compilerexplorer.common.Bundle;
import com.compilerexplorer.common.Tabs;
import com.compilerexplorer.common.component.DataHolder;
import com.compilerexplorer.datamodel.CompilerResult;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public abstract class BasePreprocessorUtilProvider extends BaseTabProvider {
    private final boolean showError;

    public BasePreprocessorUtilProvider(@NotNull Project project, @NotNull Tabs tab, @NonNls @NotNull String actionId, @NotNull FileType fileType, boolean showError_) {
        super(project, tab, actionId, fileType);
        showError = showError_;
    }

    @Override
    public boolean isEnabled(@NotNull DataHolder data) {
        return shouldShow(data);
    }

    @Override
    public boolean isError(@NotNull DataHolder data) {
        return shouldShowError(data);
    }

    protected abstract boolean shouldShow(@NotNull DataHolder data);

    protected boolean shouldShowError(@NotNull DataHolder data) {
        return showError && shouldShow(data);
    }

    @Nls
    @NotNull
    protected static String getPreprocessorErrorMessage(@NotNull CompilerResult.Output output) {
        StringBuilder errorMessageBuilder = new StringBuilder();
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
}
