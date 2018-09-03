package com.compilerexplorer.settings;

import com.compilerexplorer.common.CompilerExplorerState;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

@State(name = "CompilerExplorerSettingsProvider", storages = @Storage(file = "compilerexplorer.settings.xml"))
public class CompilerExplorerSettingsProvider implements PersistentStateComponent<CompilerExplorerState> {

    @NotNull
    private final Project project;
    private CompilerExplorerState state;

    public static CompilerExplorerSettingsProvider getInstance(@NotNull Project project) {
        return ServiceManager.getService(project, CompilerExplorerSettingsProvider.class);
    }

    public CompilerExplorerSettingsProvider(@NotNull Project project_) {
        project = project_;
    }

    @NotNull
    Project getProject() {
        return project;
    }

    @Override
    @NotNull
    public CompilerExplorerState getState() {
        createStateIfNeeded();
        return state;
    }

    @Override
    public void loadState(@NotNull CompilerExplorerState state_) {
        createStateIfNeeded();
        state.copyFrom(state_);
        connectIfNeeded();
    }

    private void createStateIfNeeded() {
        if (state == null) {
            state = new CompilerExplorerState();
        }
    }

    private void connectIfNeeded() {
        if (!state.getConnected() && state.getLastConnectionStatus().isEmpty()) {
            CompilerExplorerConnection.connect(project, state);
        }
    }
}