package com.compilerexplorer.common;

import com.compilerexplorer.datamodel.state.SettingsState;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

@State(name = "CompilerExplorerSettingsProvider", storages = @Storage(value = "compilerexplorer.settings.xml"))
public class CompilerExplorerSettingsProvider implements PersistentStateComponent<SettingsState> {
    @NotNull
    private final Project project;
    @NotNull
    private final SettingsState state;
    @Nullable
    private Consumer<RefreshSignal> refreshSignalConsumer;

    public static CompilerExplorerSettingsProvider getInstance(@NotNull Project project) {
        return project.getService(CompilerExplorerSettingsProvider.class);
    }

    @SuppressWarnings("WeakerAccess")
    public CompilerExplorerSettingsProvider(@NotNull Project project_) {
        project = project_;
        state = new SettingsState();
    }

    public void setRefreshSignalConsumer(@NotNull Consumer<RefreshSignal> refreshSignalConsumer_) {
        refreshSignalConsumer = refreshSignalConsumer_;
    }

    @NotNull
    public Project getProject() {
        return project;
    }

    @Override
    @NotNull
    public SettingsState getState() {
        return state;
    }

    @Override
    public void loadState(@NotNull SettingsState state_) {
        state_.setEnabled(true);
        copyFrom(state_);
    }

    public void copyFrom(@NotNull SettingsState state_) {
        boolean urlChanged = !state.getUrl().equals(state_.getUrl());
        boolean preprocessChanged = (state.getPreprocessLocally() != state_.getPreprocessLocally()) || !state.getIgnoreSwitches().equals(state_.getIgnoreSwitches());
        state.copyFrom(state_);
        if (refreshSignalConsumer != null) {
            if (urlChanged) {
                refreshSignalConsumer.accept(RefreshSignal.RECONNECT);
            } else if (preprocessChanged) {
                refreshSignalConsumer.accept(RefreshSignal.PREPROCESS);
            }
        }
    }
}