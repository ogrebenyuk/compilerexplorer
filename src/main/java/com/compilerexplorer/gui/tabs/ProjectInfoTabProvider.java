package com.compilerexplorer.gui.tabs;

import com.compilerexplorer.common.Tabs;
import com.compilerexplorer.datamodel.CompiledText;
import com.compilerexplorer.datamodel.json.JsonSerializationVisitor;
import com.intellij.json.JsonFileType;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class ProjectInfoTabProvider extends TabProvider {
    public ProjectInfoTabProvider(@NotNull Project project) {
        super(project, Tabs.PROJECT_INFO, "compilerexplorer.ShowProjectInfoTab", JsonFileType.INSTANCE);
    }

    @Override
    public boolean isEnabled(@NotNull CompiledText compiledText) {
        return compiledText.sourceRemoteMatched.preprocessedSource.sourceCompilerSettings.sourceSettingsConnected.sourceSettings.projectSettings.getSettings().isEmpty();
    }

    @Override
    public boolean isError(@NotNull CompiledText compiledText) {
        return !compiledText.sourceRemoteMatched.preprocessedSource.sourceCompilerSettings.sourceSettingsConnected.sourceSettings.projectSettings.getSettings().isEmpty();
    }

    @Override
    public void provide(@NotNull CompiledText compiledText, @NotNull Function<String, EditorEx> textConsumer) {
        if (!compiledText.sourceRemoteMatched.preprocessedSource.sourceCompilerSettings.sourceSettingsConnected.sourceSettings.projectSettings.getSettings().isEmpty()) {
            JsonSerializationVisitor gsonSerializer = JsonSerializationVisitor.createDebugSerializer();
            compiledText.sourceRemoteMatched.preprocessedSource.sourceCompilerSettings.sourceSettingsConnected.sourceSettings.projectSettings.accept(gsonSerializer);
            textConsumer.apply(gsonSerializer.output());
        } else {
            String errorMessage = "No sources in this project";
            textConsumer.apply(errorMessage);
        }
    }
}
