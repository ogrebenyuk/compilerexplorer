package com.compilerexplorer.gui.tabs;

import com.compilerexplorer.common.Bundle;
import com.compilerexplorer.common.Tabs;
import com.compilerexplorer.common.component.DataHolder;
import com.compilerexplorer.datamodel.CompilerResult;
import com.compilerexplorer.datamodel.SelectedSource;
import com.compilerexplorer.datamodel.SelectedSourceCompiler;
import com.compilerexplorer.datamodel.state.SettingsState;
import com.compilerexplorer.gui.json.JsonSerializer;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class PreprocessorVersionOutputTabProvider extends BasePreprocessorVersionTabProvider {
    public PreprocessorVersionOutputTabProvider(@NotNull SettingsState state) {
        super(state, Tabs.PREPROCESSOR_VERSION_OUTPUT, "compilerexplorer.ShowPreprocessorVersionOutputTab", true, unused -> "");
    }

    @Override
    public void provide(@NotNull DataHolder data, @NotNull TabContentConsumer contentConsumer) {
        data.get(SelectedSourceCompiler.KEY).ifPresentOrElse(
                selectedSourceCompiler -> {
                    if (selectedSourceCompiler.getIsSupportedCompilerType()) {
                        if (selectedSourceCompiler.getCanceled()) {
                            error(true, () -> Bundle.get("compilerexplorer.PreprocessorVersionOutputTabProvider.Canceled"), contentConsumer);
                        } else {
                            badOutput(selectedSourceCompiler).ifPresentOrElse(
                                    output -> error(true, () -> getPreprocessorErrorMessage(selectedSourceCompiler.getResult().orElse(null), output), contentConsumer),
                                    () -> content(false, () -> JsonSerializer.createSerializer().toJson(selectedSourceCompiler), contentConsumer)
                            );
                        }
                    } else {
                        error(true, () -> Bundle.format("compilerexplorer.PreprocessorVersionOutputTabProvider.Unsupported", "CompilerKind", getCompilerKind(data)), contentConsumer);
                    }
                },
                () -> message(() -> Bundle.get("compilerexplorer.PreprocessorVersionOutputTabProvider.WasNotRun"), contentConsumer)
        );
    }

    @NotNull
    private static Optional<CompilerResult.Output> badOutput(@NotNull SelectedSourceCompiler selectedSourceCompiler) {
        return selectedSourceCompiler.getResult().flatMap(CompilerResult::getOutput).filter(output -> badOutput(selectedSourceCompiler, output));
    }

    private static boolean badOutput(@NotNull SelectedSourceCompiler selectedSourceCompiler, @NotNull CompilerResult.Output output) {
        return output.getException().isPresent() || (output.getExitCode() != 0 && !selectedSourceCompiler.getCanceled());
    }

    @NonNls
    @NotNull
    private static String getCompilerKind(@NotNull DataHolder data) {
        return data.get(SelectedSource.KEY).map(source -> source.getSelectedSource().compilerKind).orElse("");
    }
}
