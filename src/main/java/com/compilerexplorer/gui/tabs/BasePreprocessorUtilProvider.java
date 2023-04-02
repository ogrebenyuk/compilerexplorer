package com.compilerexplorer.gui.tabs;

import com.compilerexplorer.common.Tabs;
import com.compilerexplorer.common.component.DataHolder;
import com.compilerexplorer.datamodel.CompilerResult;
import com.compilerexplorer.datamodel.SelectedSourceCompiler;
import com.intellij.json.JsonFileType;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.PlainTextFileType;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.function.BiFunction;
import java.util.function.Function;

public abstract class BasePreprocessorUtilProvider extends BaseTabProvider {
    private final boolean showError;

    public BasePreprocessorUtilProvider(@NotNull Project project, @NotNull Tabs tab, @NotNull String actionId, @NotNull FileType fileType, boolean showError_) {
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

    @NotNull
    protected static String getPreprocessorErrorMessage(@NotNull CompilerResult.Output output) {
        StringBuilder errorMessageBuilder = new StringBuilder();
        output.getException().ifPresentOrElse(
                exception -> errorMessageBuilder.append("Error: " + exception.getMessage() + "\n"),
                () -> {
                    if (output.getExitCode() != 0) {
                        errorMessageBuilder.append("Exit code: " + output.getExitCode() + "\n");
                    }
                    errorMessageBuilder.append(output.getStderr());
                }
        );
        return errorMessageBuilder.toString();
    }
}
