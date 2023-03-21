package com.compilerexplorer.compiler;

import com.compilerexplorer.common.CompilerExplorerSettingsProvider;
import com.compilerexplorer.common.RefreshSignal;
import com.compilerexplorer.datamodel.SourceRemoteMatched;
import com.compilerexplorer.datamodel.state.LocalCompilerPath;
import com.compilerexplorer.datamodel.state.SettingsState;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Function;

public class SourceRemoteMatchSaver<T> implements Consumer<T> {
    @NotNull
    private final Project project;
    @NotNull
    private final Consumer<T> sourceRemoteMatchedConsumer;
    @NotNull
    private final Function<T, SourceRemoteMatched> producer;

    public SourceRemoteMatchSaver(@NotNull Project project_,
                                  @NotNull Consumer<T> sourceRemoteMatchedConsumer_,
                                  @NotNull Function<T, SourceRemoteMatched> producer_) {
        project = project_;
        sourceRemoteMatchedConsumer = sourceRemoteMatchedConsumer_;
        producer = producer_;
    }

    @Override
    public void accept(@NotNull T sourceRemoteMatched) {
        SettingsState state = CompilerExplorerSettingsProvider.getInstance(project).getState();

        if (!state.getEnabled()) {
            return;
        }

        state.getCompilerMatches().put(new LocalCompilerPath(producer.apply(sourceRemoteMatched).getSourceCompilerSettings().getSourceSettings().getCompilerPath()), producer.apply(sourceRemoteMatched).getRemoteCompilerMatches());
        sourceRemoteMatchedConsumer.accept(sourceRemoteMatched);
    }

    @NotNull
    public Consumer<RefreshSignal> asRefreshSignalConsumer() {
        return refreshSignal -> {
            SettingsState state = CompilerExplorerSettingsProvider.getInstance(project).getState();
            state.setCompilerMatches(SettingsState.EMPTY.getCompilerMatches());
        };
    }
}
