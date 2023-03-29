package com.compilerexplorer.gui.tabs;

import com.compilerexplorer.common.Tabs;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Function;

import static com.compilerexplorer.datamodel.PreprocessedSource.*;

public abstract class PreprocessorTabProvider extends TabProvider {
    public PreprocessorTabProvider(@NotNull Project project_, @NotNull Tabs tab_, @NotNull String actionId_, @NotNull FileType fileType_) {
        super(project_, tab_, actionId_, fileType_);
    }

    protected void showPreprocessorError(@Nullable Exception exception,
                                         int exitCode,
                                         @Nullable String stderr,
                                         @NotNull Function<String, EditorEx> textConsumer) {
        StringBuilder errorMessageBuilder = new StringBuilder();
        if (exception != null) {
            errorMessageBuilder.append("Error: " + exception.getMessage() + "\n");
        }
        if (exitCode != CODE_NOT_PREPROCESSED && exitCode != CODE_GOOD && exitCode != CODE_REGULAR_BAD) {
            errorMessageBuilder.append("Preprocessor exit code: " + exitCode + "\n");
        }
        if (stderr != null) {
            errorMessageBuilder.append(stderr);
        }
        textConsumer.apply(errorMessageBuilder.toString());
    }
}
