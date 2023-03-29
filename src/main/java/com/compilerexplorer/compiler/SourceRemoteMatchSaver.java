package com.compilerexplorer.compiler;

import com.compilerexplorer.common.CompilerExplorerSettingsProvider;
import com.compilerexplorer.common.RefreshSignal;
import com.compilerexplorer.datamodel.SourceRemoteMatched;
import com.compilerexplorer.datamodel.state.LocalCompilerPath;
import com.compilerexplorer.datamodel.state.SettingsState;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class SourceRemoteMatchSaver implements Consumer<SourceRemoteMatched> {
    @NotNull
    private final Project project;
    @NotNull
    private final Consumer<SourceRemoteMatched> delegate;

    public SourceRemoteMatchSaver(@NotNull Project project_, @NotNull Consumer<SourceRemoteMatched> delegate_) {
        project = project_;
        delegate = delegate_;
    }

    @Override
    public void accept(@NotNull SourceRemoteMatched sourceRemoteMatched) {
        SettingsState state = CompilerExplorerSettingsProvider.getInstance(project).getState();

        if (!state.getEnabled()) {
            return;
        }

        if (sourceRemoteMatched.isValid()) {
            assert sourceRemoteMatched.preprocessedSource.sourceCompilerSettings.sourceSettingsConnected.sourceSettings.selectedSourceSettings != null;

            state.getCompilerMatches().put(new LocalCompilerPath(sourceRemoteMatched.preprocessedSource.sourceCompilerSettings.sourceSettingsConnected.sourceSettings.selectedSourceSettings.compilerPath), sourceRemoteMatched.remoteCompilerMatches);
        }

        delegate.accept(sourceRemoteMatched);
    }

    @NotNull
    public Consumer<RefreshSignal> asReconnectSignalConsumer() {
        return refreshSignal -> {
            SettingsState state = CompilerExplorerSettingsProvider.getInstance(project).getState();
            state.setCompilerMatches(SettingsState.EMPTY.getCompilerMatches());
        };
    }
}
