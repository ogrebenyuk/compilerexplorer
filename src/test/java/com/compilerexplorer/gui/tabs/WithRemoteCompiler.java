package com.compilerexplorer.gui.tabs;

import com.compilerexplorer.common.Tabs;
import com.compilerexplorer.common.component.DataHolder;
import com.compilerexplorer.datamodel.CompiledText;
import com.compilerexplorer.datamodel.PreprocessedSource;
import com.compilerexplorer.datamodel.SourceRemoteMatched;
import com.compilerexplorer.datamodel.state.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.compilerexplorer.gui.tabs.WithMatch.Status.MATCHED;
import static com.compilerexplorer.gui.tabs.WithPreprocessor.Status.GOOD_RUN;

public interface WithRemoteCompiler extends WithPreprocessor, WithMatch {
    enum Status {CANCELED, EXCEPTION, BAD_RUN, GOOD_RUN, WITH_STDOUT, WITH_STDERR, CANCELED_EXEC, EXCEPTION_EXEC, BAD_RUN_EXEC, GOOD_RUN_EXEC, GOOD_WITH_EXCEPTION}

    @NotNull
    default DataHolder dataWithRemoteCompiler(@NotNull Status status) {
        DataHolder data = withPreprocessor(dataWithMatch(MATCHED), GOOD_RUN);
        CompiledText.CompiledResult badRunResult = new CompiledText.CompiledResult();
        badRunResult.code = 1;
        CompiledText.CompiledResult goodRunResult = new CompiledText.CompiledResult();
        goodRunResult.code = 0;
        CompiledText.CompiledChunk output = new CompiledText.CompiledChunk();
        output.text = "myOutput";
        CompiledText.CompiledResult goodRunResultWithStdout = new CompiledText.CompiledResult();
        goodRunResultWithStdout.code = 0;
        goodRunResultWithStdout.stdout = List.of(output);
        CompiledText.CompiledResult goodRunResultWithStderr = new CompiledText.CompiledResult();
        goodRunResultWithStderr.code = 0;
        goodRunResultWithStderr.stderr = List.of(output);
        CompiledText.CompiledResult goodRunResultWithExec = new CompiledText.CompiledResult();
        goodRunResultWithExec.code = 0;
        goodRunResultWithExec.execResult = new CompiledText.ExecResult();
        goodRunResultWithExec.execResult.didExecute = true;
        goodRunResultWithExec.execResult.stdout = List.of(output);
        data.get(PreprocessedSource.KEY).flatMap(preprocessedSource -> data.get(SourceRemoteMatched.SELECTED_KEY)).ifPresent(match ->
            data.put(CompiledText.KEY, switch (status) {
                case CANCELED, CANCELED_EXEC -> new CompiledText(true, "rawInput", "", null, null);
                case EXCEPTION, EXCEPTION_EXEC -> new CompiledText(false, "rawInput", "", new RuntimeException(), null);
                case BAD_RUN, BAD_RUN_EXEC -> new CompiledText(false, "rawInput", "", null, badRunResult);
                case GOOD_RUN -> new CompiledText(false, "rawInput", "", null, goodRunResult);
                case WITH_STDOUT -> new CompiledText(false, "rawInput", "", null, goodRunResultWithStdout);
                case WITH_STDERR -> new CompiledText(false, "rawInput", "", null, goodRunResultWithStderr);
                case GOOD_RUN_EXEC -> new CompiledText(false, "rawInput", "", null, goodRunResultWithExec);
                case GOOD_WITH_EXCEPTION -> new CompiledText(false, "rawInput", "", new RuntimeException(), goodRunResult);
            })
        );
        return data;
    }

    @NotNull
    default SettingsState stateWithRemoteCompiler(@NotNull Status status) {
        SettingsState state = withPreprocessor(stateWithMatch(MATCHED), GOOD_RUN);
        if (!state.getCompilerMatches().isEmpty() && (status == Status.CANCELED_EXEC || status == Status.EXCEPTION_EXEC || status == Status.BAD_RUN_EXEC || status == Status.GOOD_RUN_EXEC)) {
            Filters filters = state.getFilters();
            filters.setExecute(true);
            state.setFilters(filters);
        }
        return state;
    }

    @NotNull
    default List<CollectedTabContent> resultWithRemoteCompiler(@NotNull Status status) {
        List<CollectedTabContent> result = withPreprocessor(resultWithMatch(MATCHED), GOOD_RUN);
        set(result, content(Tabs.EXPLORER_RAW_INPUT, DISABLED, JSON));
        set(result, content(Tabs.EXPLORER_RAW_OUTPUT, DISABLED, JSON));
        set(result, switch(status) {
            case CANCELED, CANCELED_EXEC, EXCEPTION, EXCEPTION_EXEC, BAD_RUN, BAD_RUN_EXEC, GOOD_WITH_EXCEPTION -> content(Tabs.EXPLORER_STDOUT, ENABLED, TEXT);
            case GOOD_RUN, WITH_STDOUT, WITH_STDERR, GOOD_RUN_EXEC -> content(Tabs.EXPLORER_STDOUT, DISABLED, TEXT);
        });
        set(result, switch(status) {
            case CANCELED, CANCELED_EXEC, EXCEPTION, EXCEPTION_EXEC, BAD_RUN, BAD_RUN_EXEC, GOOD_WITH_EXCEPTION -> content(Tabs.EXPLORER_STDERR, ENABLED, TEXT);
            case GOOD_RUN, WITH_STDOUT, WITH_STDERR, GOOD_RUN_EXEC -> content(Tabs.EXPLORER_STDERR, DISABLED, TEXT);
        });
        set(result, switch(status) {
            case CANCELED, CANCELED_EXEC -> error(Tabs.EXPLORER_OUTPUT, ENABLED, "compilerexplorer.ExplorerOutputTabProvider.Canceled");
            case EXCEPTION, EXCEPTION_EXEC, GOOD_WITH_EXCEPTION -> error(Tabs.EXPLORER_OUTPUT, ENABLED, "compilerexplorer.BaseExplorerUtilProvider.Exception");
            case BAD_RUN, BAD_RUN_EXEC -> error(Tabs.EXPLORER_OUTPUT, ENABLED, "compilerexplorer.BaseExplorerUtilProvider.ExitCode");
            case GOOD_RUN, WITH_STDOUT, WITH_STDERR, GOOD_RUN_EXEC -> contentWithFolding(Tabs.EXPLORER_OUTPUT, ENABLED, ASM);
        });
        set(result, message(Tabs.EXPLORER_OUTPUT_DEVICE_1, DISABLED, "compilerexplorer.BaseExplorerOutputDeviceTabProvider.NoDevice"));
        set(result, message(Tabs.EXPLORER_OUTPUT_DEVICE_2, DISABLED, "compilerexplorer.BaseExplorerOutputDeviceTabProvider.NoDevice"));
        set(result, message(Tabs.EXPLORER_OUTPUT_DEVICE_3, DISABLED, "compilerexplorer.BaseExplorerOutputDeviceTabProvider.NoDevice"));
        set(result, message(Tabs.EXPLORER_OUTPUT_DEVICE_4, DISABLED, "compilerexplorer.BaseExplorerOutputDeviceTabProvider.NoDevice"));
        set(result, message(Tabs.EXPLORER_OUTPUT_DEVICE_5, DISABLED, "compilerexplorer.BaseExplorerOutputDeviceTabProvider.NoDevice"));
        set(result, switch(status) {
            case CANCELED, EXCEPTION, BAD_RUN, GOOD_RUN, WITH_STDOUT, WITH_STDERR, GOOD_WITH_EXCEPTION -> message(Tabs.EXPLORER_EXEC_RESULT, DISABLED, "compilerexplorer.ExplorerExecResultTabProvider.Disabled");
            case CANCELED_EXEC ->  error(Tabs.EXPLORER_EXEC_RESULT, ENABLED, "compilerexplorer.ExplorerExecResultTabProvider.Canceled");
            case EXCEPTION_EXEC -> error(Tabs.EXPLORER_EXEC_RESULT, ENABLED, "compilerexplorer.BaseExplorerUtilProvider.Exception");
            case BAD_RUN_EXEC ->   error(Tabs.EXPLORER_EXEC_RESULT, ENABLED, "compilerexplorer.BaseExplorerUtilProvider.ExitCode");
            case GOOD_RUN_EXEC -> content(Tabs.EXPLORER_EXEC_RESULT, ENABLED, TEXT);
        });
        return result;
    }
}
