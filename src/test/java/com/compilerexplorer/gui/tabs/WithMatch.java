package com.compilerexplorer.gui.tabs;

import com.compilerexplorer.common.component.DataHolder;
import com.compilerexplorer.datamodel.RemoteCompilersOutput;
import com.compilerexplorer.datamodel.SelectedSourceCompiler;
import com.compilerexplorer.datamodel.SourceRemoteMatched;
import com.compilerexplorer.datamodel.state.*;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.compilerexplorer.gui.tabs.WithPreprocessorVersion.Status.GOOD_RUN;
import static com.compilerexplorer.gui.tabs.WithRemoteCompilers.Status.ALREADY_CONNECTED;

public interface WithMatch extends WithPreprocessorVersion, WithRemoteCompilers {
    enum Status {NOT_MATCHED, CACHED, MATCHED}
    @NotNull
    CompilerMatches MATCHES = new CompilerMatches(new CompilerMatch(remoteCompilerInfo(), CompilerMatchKind.EXACT_MATCH), List.of());

    @NotNull
    default DataHolder dataWithMatch(@NotNull Status status) {
        DataHolder data = withRemoteCompilers(dataWithPreprocessorVersion(GOOD_RUN), ALREADY_CONNECTED);
        if (status != Status.NOT_MATCHED) {
            data.get(SelectedSourceCompiler.KEY).flatMap(selectedSourceCompiler -> data.get(RemoteCompilersOutput.KEY)).ifPresent(remoteCompilersOutput ->
                data.put(SourceRemoteMatched.SELECTED_KEY, new SourceRemoteMatched(status == Status.CACHED, MATCHES))
            );
        }
        return data;
    }

    @NotNull
    default SettingsState stateWithMatch(@NotNull Status status) {
        SettingsState state = withRemoteCompilers(stateWithPreprocessorVersion(GOOD_RUN), ALREADY_CONNECTED);
        if (!state.getLocalCompilerSettings().isEmpty() && status != Status.NOT_MATCHED) {
            state.addToCompilerMatches(new LocalCompilerPath("compilerPath"), MATCHES);
        }
        return state;
    }

    @NotNull
    default List<CollectedTabContent> resultWithMatch(@NotNull Status status) {
        return withRemoteCompilers(resultWithPreprocessorVersion(GOOD_RUN), ALREADY_CONNECTED);
    }

    @NotNull
    static RemoteCompilerInfo remoteCompilerInfo() {
        RemoteCompilerInfo match = new RemoteCompilerInfo();
        match.setId("id");
        return match;
    }
}
