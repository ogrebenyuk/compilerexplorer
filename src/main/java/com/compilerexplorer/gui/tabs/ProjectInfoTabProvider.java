package com.compilerexplorer.gui.tabs;

import com.compilerexplorer.common.Bundle;
import com.compilerexplorer.common.Tabs;
import com.compilerexplorer.common.component.DataHolder;
import com.compilerexplorer.datamodel.ProjectSources;
import com.compilerexplorer.datamodel.state.SettingsState;
import com.compilerexplorer.gui.json.JsonSerializer;
import com.intellij.json.JsonFileType;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class ProjectInfoTabProvider extends BaseTabProvider {
    public ProjectInfoTabProvider(@NotNull SettingsState state) {
        super(state, Tabs.PROJECT_INFO, "compilerexplorer.ShowProjectInfoTab", JsonFileType.INSTANCE);
    }

    @Override
    public boolean isSourceSpecific() {
        return false;
    }

    @Override
    public void provide(@NotNull DataHolder data, @NotNull TabContentConsumer contentConsumer) {
        sources(data).ifPresentOrElse(
                sources -> content(false, () -> JsonSerializer.createSerializer().toJson(sources), contentConsumer),
                () -> error(true, () -> Bundle.get("compilerexplorer.ProjectInfoTabProvider.NoSources"), contentConsumer)
        );
    }

    @NotNull
    private static Optional<ProjectSources> sources(@NotNull DataHolder data) {
        return data.get(ProjectSources.KEY).filter(sources -> !sources.getSources().isEmpty());
    }
}
