package com.compilerexplorer.gui.tabs;

import com.compilerexplorer.common.Tabs;
import com.compilerexplorer.common.component.DataHolder;
import com.compilerexplorer.datamodel.CompilerResult;
import com.compilerexplorer.datamodel.SelectedSourceCompiler;
import com.compilerexplorer.gui.json.JsonSerializer;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Consumer;

public class PreprocessorVersionOutputTabProvider extends BasePreprocessorVersionTabProvider {
    public PreprocessorVersionOutputTabProvider(@NotNull Project project) {
        super(project, Tabs.PREPROCESSOR_VERSION_OUTPUT, "compilerexplorer.ShowPreprocessorVersionOutputTab", true, true, (unused1, unused2) -> "");
    }

    @Override
    public void provide(@NotNull DataHolder data, @NotNull Consumer<String> textConsumer) {
        data.get(SelectedSourceCompiler.KEY).ifPresentOrElse(
                selectedSourceCompiler -> textConsumer.accept(badOutput(selectedSourceCompiler)
                        .map(BasePreprocessorUtilProvider::getPreprocessorErrorMessage)
                        .orElse(JsonSerializer.createSerializer().toJson(selectedSourceCompiler))
                ),
                () -> textConsumer.accept("Preprocessor was not run")
        );
    }

    @Override
    protected boolean shouldShow(@NotNull DataHolder data) {
        return super.shouldShow(data) || canceled(data);
    }

    @Override
    protected boolean shouldShowError(@NotNull DataHolder data) {
        return super.shouldShow(data);
    }

    @NotNull
    private static Optional<CompilerResult.Output> badOutput(@NotNull SelectedSourceCompiler selectedSourceCompiler) {
        return selectedSourceCompiler.getResult().flatMap(CompilerResult::getOutput).filter(output -> badOutput(selectedSourceCompiler, output));
    }

    private static boolean badOutput(@NotNull SelectedSourceCompiler selectedSourceCompiler, @NotNull CompilerResult.Output output) {
        return output.getException().isPresent() || (output.getExitCode() != 0 && !selectedSourceCompiler.getCanceled());
    }

    private boolean canceled(@NotNull DataHolder data) {
        return data.get(SelectedSourceCompiler.KEY).map(SelectedSourceCompiler::getCanceled).orElse(false);
    }

    @Override
    protected boolean producedNoResult(@NotNull SelectedSourceCompiler selectedSourceCompiler) {
        return badOutput(selectedSourceCompiler).isPresent();
    }
}
