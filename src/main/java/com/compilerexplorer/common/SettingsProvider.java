package com.compilerexplorer.common;

import com.compilerexplorer.common.datamodel.state.SettingsState;
import com.compilerexplorer.common.datamodel.state.StateConsumer;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.ServiceManager;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

@State(name = "SettingsProvider", storages = @Storage(file = "compilerexplorer.settings.xml"))
public class SettingsProvider implements PersistentStateComponent<SettingsState> {
    @NotNull
    private final Project project;
    @Nullable
    private SettingsState state;

    public static SettingsProvider getInstance(@NotNull Project project) {
        return ServiceManager.getService(project, SettingsProvider.class);
    }

    public SettingsProvider(@NotNull Project project_) {
        project = project_;
    }

    @NotNull
    public Project getProject() {
        return project;
    }

    @Override
    @NotNull
    public SettingsState getState() {
        state = createStateIfNeeded();
        if (state.isConnectionCleared()) {
            RemoteConnection.connect(project, state);
        }
        return state;
    }

    @Override
    public void loadState(@NotNull SettingsState state_) {
        state = createStateIfNeeded();
        state.copyFrom(state_);
        publishStateChangedLater();
    }

    @NotNull
    private SettingsState createStateIfNeeded() {
        if (state == null) {
            state = new SettingsState();
        }
        return state;
    }

    private void publishStateChangedLater() {
        ApplicationManager.getApplication().invokeLater(() -> project.getMessageBus().syncPublisher(StateConsumer.TOPIC).stateChanged());
    }
}