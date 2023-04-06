package com.compilerexplorer.gui.tabs;

import com.compilerexplorer.common.Constants;
import com.compilerexplorer.common.Tabs;
import com.compilerexplorer.common.component.DataHolder;
import com.compilerexplorer.datamodel.CompilerResult;
import com.compilerexplorer.datamodel.PreprocessedSource;
import com.compilerexplorer.datamodel.SelectedSource;
import com.compilerexplorer.datamodel.state.SettingsState;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface WithPreprocessor extends WithSelectedSource {
    enum Status {DISABLED, CANCELED, EXCEPTION, BAD_RUN, GOOD_RUN}

    @NotNull
    default DataHolder withPreprocessor(@NotNull DataHolder data, @NotNull Status status) {
        CompilerResult.Output canceledOutput = new CompilerResult.Output(-1, "", "", null);
        CompilerResult.Output outputWithException = new CompilerResult.Output(-1, "", "", new RuntimeException());
        CompilerResult.Output badRunOutput = new CompilerResult.Output(1, "", "", null);
        CompilerResult.Output goodRunOutput = new CompilerResult.Output(0, "", "", null);
        CompilerResult canceledResult = new CompilerResult("compilerWorkingDir", new String[] {"preprocessorCommandLine"}, canceledOutput);
        CompilerResult resultWithException = new CompilerResult("compilerWorkingDir", new String[] {"preprocessorCommandLine"}, outputWithException);
        CompilerResult badRunResult = new CompilerResult("compilerWorkingDir", new String[] {"preprocessorCommandLine"}, badRunOutput);
        CompilerResult goodRunResult = new CompilerResult("compilerWorkingDir", new String[] {"preprocessorCommandLine"}, goodRunOutput);
        data.get(SelectedSource.KEY).ifPresent(selectedSource ->
            data.put(PreprocessedSource.KEY, switch(status) {
                case DISABLED ->    new PreprocessedSource(false, false, null, "sourceText");
                case CANCELED ->    new PreprocessedSource(true, true, canceledResult, null);
                case EXCEPTION ->   new PreprocessedSource(true, false, resultWithException, null);
                case BAD_RUN ->     new PreprocessedSource(true, false, badRunResult, null);
                case GOOD_RUN ->    new PreprocessedSource(true, false, goodRunResult, "preprocessedText");
            })
        );
        return data;
    }

    @NotNull
    default DataHolder dataWithPreprocessor(@NotNull Status status) {
        return withPreprocessor(dataWithSelectedSource(WithSelectedSource.Status.WITH_SELECTION), status);
    }

    @NotNull
    default SettingsState withPreprocessor(@NotNull SettingsState state, @NotNull Status status) {
        state.setPreprocessLocally(status != Status.DISABLED);
        return state;
    }

    @NotNull
    default SettingsState stateWithPreprocessor(@NotNull Status status) {
        return withPreprocessor(stateWithSelectedSource(WithSelectedSource.Status.WITH_SELECTION), status);
    }

    @NotNull
    default List<CollectedTabContent> withPreprocessor(@NotNull List<CollectedTabContent> result, @NotNull Status status) {
        set(result, switch (status) {
            case DISABLED ->  message(Tabs.PREPROCESSOR_STDOUT, DISABLED, "compilerexplorer.BasePreprocessorTabProvider.Disabled");
            case CANCELED, EXCEPTION, BAD_RUN -> content(Tabs.PREPROCESSOR_STDOUT, ENABLED, SOURCE, Constants.DEFAULT_PREPROCESSED_TEXT_EXTENSION);
            case GOOD_RUN ->  content(Tabs.PREPROCESSOR_STDOUT, DISABLED, SOURCE, Constants.DEFAULT_PREPROCESSED_TEXT_EXTENSION);
        });
        set(result, switch (status) {
            case DISABLED ->  message(Tabs.PREPROCESSOR_STDERR, DISABLED, "compilerexplorer.BasePreprocessorTabProvider.Disabled");
            case CANCELED, EXCEPTION, BAD_RUN -> content(Tabs.PREPROCESSOR_STDERR, ENABLED, TEXT);
            case GOOD_RUN ->  content(Tabs.PREPROCESSOR_STDERR, DISABLED, TEXT);
        });
        set(result, switch (status) {
            case DISABLED ->  message(Tabs.PREPROCESSOR_OUTPUT, DISABLED, "compilerexplorer.BasePreprocessorTabProvider.Disabled");
            case CANCELED ->  error(Tabs.PREPROCESSOR_OUTPUT,   ENABLED, "compilerexplorer.BasePreprocessorTabProvider.Canceled");
            case EXCEPTION -> error(Tabs.PREPROCESSOR_OUTPUT,   ENABLED, "compilerexplorer.BasePreprocessorUtilProvider.Exception");
            case BAD_RUN ->   error(Tabs.PREPROCESSOR_OUTPUT,   ENABLED, "compilerexplorer.BasePreprocessorUtilProvider.ExitCode");
            case GOOD_RUN ->  content(Tabs.PREPROCESSOR_OUTPUT, ENABLED, SOURCE, Constants.DEFAULT_PREPROCESSED_TEXT_EXTENSION);
        });
        return result;
    }

    @NotNull
    default List<CollectedTabContent> resultWithPreprocessor(@NotNull Status status) {
        return withPreprocessor(resultWithSelectedSource(WithSelectedSource.Status.WITH_SELECTION), status);
    }
}
