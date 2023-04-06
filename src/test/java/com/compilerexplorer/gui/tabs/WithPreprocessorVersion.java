package com.compilerexplorer.gui.tabs;

import com.compilerexplorer.common.Tabs;
import com.compilerexplorer.common.component.DataHolder;
import com.compilerexplorer.datamodel.*;
import com.compilerexplorer.datamodel.state.LocalCompilerPath;
import com.compilerexplorer.datamodel.state.LocalCompilerSettings;
import com.compilerexplorer.datamodel.state.SettingsState;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface WithPreprocessorVersion extends WithSelectedSource {
    enum Status {CACHED, UNSUPPORTED, CANCELED, EXCEPTION, BAD_RUN, GOOD_RUN}
    @NotNull
    LocalCompilerSettings SETTINGS = new LocalCompilerSettings("compilerName", "compilerVersion", "compilerTarget");

    @NotNull
    default DataHolder withPreprocessorVersion(@NotNull DataHolder data, @NotNull Status status) {
        CompilerResult.Output badOutputWithoutException = new CompilerResult.Output(-1, "", "", null);
        CompilerResult.Output outputWithException = new CompilerResult.Output(-1, "", "", new RuntimeException());
        CompilerResult.Output badRunOutput = new CompilerResult.Output(1, "", "", null);
        CompilerResult.Output goodRunOutput = new CompilerResult.Output(0, "", "", null);
        CompilerResult resultWithoutOutput = new CompilerResult("compilerWorkingDir", new String[]{"versionCommandLine"}, null);
        CompilerResult badResultWithoutException = new CompilerResult("compilerWorkingDir", new String[]{"versionCommandLine"}, badOutputWithoutException);
        CompilerResult resultWithException = new CompilerResult("compilerWorkingDir", new String[]{"versionCommandLine"}, outputWithException);
        CompilerResult badRunResult = new CompilerResult("compilerWorkingDir", new String[]{"versionCommandLine"}, badRunOutput);
        CompilerResult goodRunResult = new CompilerResult("compilerWorkingDir", new String[]{"versionCommandLine"}, goodRunOutput);
        LocalCompilerSettings settings = new LocalCompilerSettings("compilerKind", "compilerVersion", "compilerTarget");
        data.get(SelectedSource.KEY).ifPresent(selectedSource ->
            data.put(SelectedSourceCompiler.KEY, switch(status) {
                case CACHED ->      new SelectedSourceCompiler(true, false, true, null, SETTINGS);
                case UNSUPPORTED -> new SelectedSourceCompiler(false, false, false, resultWithoutOutput, null);
                case CANCELED ->    new SelectedSourceCompiler(false, true, true, badResultWithoutException, null);
                case EXCEPTION ->   new SelectedSourceCompiler(false, false, true, resultWithException, null);
                case BAD_RUN ->     new SelectedSourceCompiler(false, false, true, badRunResult, null);
                case GOOD_RUN ->    new SelectedSourceCompiler(false, false, true, goodRunResult, settings);
            })
        );
        return data;
    }

    @NotNull
    default DataHolder dataWithPreprocessorVersion(@NotNull Status status) {
        return withPreprocessorVersion(dataWithSelectedSource(WithSelectedSource.Status.WITH_SELECTION), status);
    }

    @NotNull
    default SettingsState withPreprocessorVersion(@NotNull SettingsState state, @NotNull Status status) {
        if (status == Status.CACHED || status == Status.GOOD_RUN) {
            state.addToLocalCompilerSettings(new LocalCompilerPath("compilerPath"), SETTINGS);
        }
        return state;
    }

    @NotNull
    default SettingsState stateWithPreprocessorVersion(@NotNull Status status) {
        return withPreprocessorVersion(stateWithSelectedSource(WithSelectedSource.Status.WITH_SELECTION), status);
    }

    @NotNull
    default List<CollectedTabContent> withPreprocessorVersion(@NotNull List<CollectedTabContent> result, @NotNull Status status) {
        set(result, switch (status) {
            case CACHED ->      message(Tabs.PREPROCESSOR_VERSION_STDOUT, DISABLED, "compilerexplorer.BasePreprocessorVersionTabProvider.Cached");
            case UNSUPPORTED, GOOD_RUN -> content(Tabs.PREPROCESSOR_VERSION_STDOUT, DISABLED, TEXT);
            case CANCELED, EXCEPTION, BAD_RUN -> content(Tabs.PREPROCESSOR_VERSION_STDOUT, ENABLED, TEXT);
        });
        set(result, switch (status) {
            case CACHED ->      message(Tabs.PREPROCESSOR_VERSION_STDERR, DISABLED, "compilerexplorer.BasePreprocessorVersionTabProvider.Cached");
            case UNSUPPORTED, GOOD_RUN -> content(Tabs.PREPROCESSOR_VERSION_STDERR, DISABLED, TEXT);
            case CANCELED, EXCEPTION, BAD_RUN -> content(Tabs.PREPROCESSOR_VERSION_STDERR, ENABLED, TEXT);
        });
        set(result, switch (status) {
            case CACHED, GOOD_RUN -> content(Tabs.PREPROCESSOR_VERSION_OUTPUT, DISABLED, JSON);
            case UNSUPPORTED -> error(Tabs.PREPROCESSOR_VERSION_OUTPUT,   ENABLED, "compilerexplorer.PreprocessorVersionOutputTabProvider.Unsupported");
            case CANCELED ->    error(Tabs.PREPROCESSOR_VERSION_OUTPUT,   ENABLED, "compilerexplorer.PreprocessorVersionOutputTabProvider.Canceled");
            case EXCEPTION ->   error(Tabs.PREPROCESSOR_VERSION_OUTPUT,   ENABLED, "compilerexplorer.BasePreprocessorUtilProvider.Exception");
            case BAD_RUN ->     error(Tabs.PREPROCESSOR_VERSION_OUTPUT,   ENABLED, "compilerexplorer.BasePreprocessorUtilProvider.ExitCode");
        });
        return result;
    }

    @NotNull
    default List<CollectedTabContent> resultWithPreprocessorVersion(@NotNull Status status) {
        return withPreprocessorVersion(resultWithSelectedSource(WithSelectedSource.Status.WITH_SELECTION), status);
    }
}
