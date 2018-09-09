package com.compilerexplorer.explorer;

import com.compilerexplorer.common.*;
import com.compilerexplorer.common.datamodel.*;
import com.compilerexplorer.common.datamodel.state.*;
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

        CompilerMatches matches = sourceRemoteMatched_.getRemoteCompilerMatches();
        state.getCompilerMatches().put(new LocalCompilerPath(sourceRemoteMatched_.getSourceCompilerSettings().getSourceSettings().getCompiler().getAbsolutePath()), matches);

        if (matches.getChosenMatch().getRemoteCompilerInfo().getId().isEmpty()) {
            String localName = sourceRemoteMatched_.getSourceCompilerSettings().getLocalCompilerSettings().getName();
            String localVersion = sourceRemoteMatched_.getSourceCompilerSettings().getLocalCompilerSettings().getVersion();
            String localTarget = sourceRemoteMatched_.getSourceCompilerSettings().getLocalCompilerSettings().getTarget();
            String language = sourceRemoteMatched_.getSourceCompilerSettings().getSourceSettings().getLanguage().getDisplayName();
            preprocessableSourceConsumer.clearPreprocessableSource("Cannot find matching remote compiler for local " + localTarget + " " + localName + " " + localVersion + " " + language + " compiler");
            return;
        }

        if (!state.getUseRemoteDefines()) {
            preprocessableSourceConsumer.setPreprocessableSource(new PreprocessableSource(sourceRemoteMatched_, new Defines()));
            return;
        }

        RemoteCompilerId match = new RemoteCompilerId(matches.getChosenMatch().getRemoteCompilerInfo().getId());
        {
            Defines existingDefines = state.getRemoteCompilerDefines().get(match);
            if (existingDefines != null) {
                preprocessableSourceConsumer.setPreprocessableSource(new PreprocessableSource(sourceRemoteMatched_, existingDefines));
                return;
            }
        }

        SettingsState tmpState = new SettingsState(state);
        tmpState.getFilters().setCommentOnly(false);
        PreprocessedSource tmpPreprocessedSource = new PreprocessedSource(new PreprocessableSource(sourceRemoteMatched_, new Defines("")), "");
        CompiledTextConsumer tmpCompiledTextConsumer = new CompiledTextConsumer() {
            @Override
            public void setCompiledText(@NotNull CompiledText compiledText) {
                Defines newDefines = getDefines(compiledText);
                state.getRemoteCompilerDefines().put(match, newDefines);
                preprocessableSourceConsumer.setPreprocessableSource(new PreprocessableSource(sourceRemoteMatched_, newDefines));
            }
            @Override
            public void clearCompiledText(@NotNull String reason) {
                preprocessableSourceConsumer.clearPreprocessableSource("Cannot get remote defines: " + reason);
            }
        };
        RemoteConnection.compile(project, tmpState.getUrl(), tmpState.getFilters(), tmpPreprocessedSource, getCompilerOptions(), tmpCompiledTextConsumer);
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
    private static Defines getDefines(@NotNull CompiledText compiledText) {
        return new Defines(compiledText.getCompiledText());
    }
}
