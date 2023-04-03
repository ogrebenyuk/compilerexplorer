package com.compilerexplorer.gui.tabs;

import com.compilerexplorer.common.Tabs;
import com.compilerexplorer.common.component.DataHolder;
import com.compilerexplorer.datamodel.ProjectSources;
import com.compilerexplorer.gui.json.JsonSerializer;
import com.intellij.json.JsonFileType;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;
import java.util.function.Consumer;

public class ProjectInfoTabProvider extends BaseTabProvider {
    public ProjectInfoTabProvider(@NotNull Project project) {
        super(project, Tabs.PROJECT_INFO, "compilerexplorer.ShowProjectInfoTab", JsonFileType.INSTANCE);
    }

    @Override
    public boolean isSourceSpecific() {
        return false;
    }

    @Override
    public boolean isEnabled(@NotNull DataHolder data) {
        return sources(data).isEmpty();
    }

    @Override
    public boolean isError(@NotNull DataHolder data) {
        return sources(data).isEmpty();
    }

    @Override
    public void provide(@NotNull DataHolder data, @NotNull Consumer<String> textConsumer) {
        sources(data).ifPresentOrElse(
                sources -> textConsumer.accept(JsonSerializer.createSerializer().toJson(sources)),
                () -> textConsumer.accept("No sources found")
        );
    }

    @NotNull
    private static Optional<ProjectSources> sources(@NotNull DataHolder data) {
        return data.get(ProjectSources.KEY).filter(sources -> !sources.getSources().isEmpty());
    }
}
