package com.compilerexplorer.explorer;

import com.compilerexplorer.common.*;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class RemoteDefinesProducer implements SourceRemoteMatchedConsumer {
    @NotNull
    private final Project project;
    @NotNull
    private final PreprocessableSourceConsumer preprocessableSourceConsumer;

    public RemoteDefinesProducer(@NotNull Project project_, @NotNull PreprocessableSourceConsumer preprocessableSourceConsumer_) {
        project = project_;
        preprocessableSourceConsumer = preprocessableSourceConsumer_;
    }

    @Override
    public void setSourceRemoteMatched(@NotNull SourceRemoteMatched sourceRemoteMatched_) {
        SettingsState state = SettingsProvider.getInstance(project).getState();

        {
            String existingDefines = state.getRemoteCompilerDefines().get(sourceRemoteMatched_.getRemoteCompilerId());
            if (existingDefines != null) {
                preprocessableSourceConsumer.setPreprocessableSource(new PreprocessableSource(sourceRemoteMatched_, existingDefines));
                return;
            }
        }

        SettingsState tmpState = new SettingsState();
        tmpState.copyFrom(state);
        tmpState.getFilters().setCommentOnly(false);
        PreprocessedSource tmpPreprocessedSource = new PreprocessedSource(new PreprocessableSource(sourceRemoteMatched_, ""), "");
        CompiledTextConsumer tmpCompiledTextConsumer = new CompiledTextConsumer() {
            @Override
            public void setCompiledText(@NotNull CompiledText compiledText) {
                String newDefines = getDefines(compiledText);
                state.getRemoteCompilerDefines().put(sourceRemoteMatched_.getRemoteCompilerId(), newDefines);
                preprocessableSourceConsumer.setPreprocessableSource(new PreprocessableSource(sourceRemoteMatched_, newDefines));
            }
            @Override
            public void clearCompiledText(@NotNull String reason) {
                preprocessableSourceConsumer.clearPreprocessableSource("Cannot get remote defines: " + reason);
            }
        };
        RemoteConnection.compile(project, tmpState, tmpPreprocessedSource, getCompilerOptions(), tmpCompiledTextConsumer);
    }

    @Override
    public void clearSourceRemoteMatched(@NotNull String reason_) {
        preprocessableSourceConsumer.clearPreprocessableSource(reason_);
    }

    @NotNull
    private static String getCompilerOptions() {
        return "-dM -E";
    }

    @NotNull
    private static String getDefines(@NotNull CompiledText compiledText) {
        return compiledText.getCompiledText();
    }
}
