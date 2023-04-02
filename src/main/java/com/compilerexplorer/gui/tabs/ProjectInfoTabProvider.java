package com.compilerexplorer.gui.tabs;

import com.compilerexplorer.common.Tabs;
import com.compilerexplorer.common.component.DataHolder;
import com.compilerexplorer.datamodel.ProjectSources;
import com.compilerexplorer.datamodel.json.JsonSerializationVisitor;
import com.intellij.json.JsonFileType;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class ProjectInfoTabProvider extends BaseTabProvider {
    public ProjectInfoTabProvider(@NotNull Project project) {
        super(project, Tabs.PROJECT_INFO, "compilerexplorer.ShowProjectInfoTab", JsonFileType.INSTANCE);
    }

    @Override
    public boolean isEnabled(@NotNull DataHolder data) {
        return !sourcesPresent(data);
    }

    @Override
    public boolean isError(@NotNull DataHolder data) {
        return !sourcesPresent(data);
    }

    @Override
    public void provide(@NotNull DataHolder data, @NotNull Function<String, EditorEx> textConsumer) {
        if (sourcesPresent(data)) {
            JsonSerializationVisitor gsonSerializer = JsonSerializationVisitor.createDebugSerializer();
            data.get(ProjectSources.KEY).ifPresent(sources -> sources.accept(gsonSerializer));
            textConsumer.apply(gsonSerializer.output());
        } else {
            String errorMessage = "No sources found";
            textConsumer.apply(errorMessage);
        }
    }

    boolean sourcesPresent(@NotNull DataHolder data) {
        return data.get(ProjectSources.KEY).map(sources -> !sources.getSources().isEmpty()).orElse(false);
    }
}
