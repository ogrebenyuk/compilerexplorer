package com.compilerexplorer.gui.tabs;

import com.compilerexplorer.common.Tabs;
import com.compilerexplorer.datamodel.CompiledText;
import com.compilerexplorer.datamodel.json.JsonSerializationVisitor;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.intellij.json.JsonFileType;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class SourceInfoTabProvider extends TabProvider {
    public SourceInfoTabProvider(@NotNull Project project) {
        super(project, Tabs.SOURCE_INFO, "compilerexplorer.ShowSourceInfoTab", JsonFileType.INSTANCE);
    }

    @Override
    public boolean isEnabled(@NotNull CompiledText compiledText) {
        return false;
    }

    @Override
    public boolean isError(@NotNull CompiledText compiledText) {
        return !compiledText.sourceRemoteMatched.isValid();
    }

    @Override
    public void provide(@NotNull CompiledText compiledText, @NotNull Function<String, EditorEx> textConsumer) {
        if (compiledText.sourceRemoteMatched.isValid()) {
            assert compiledText.sourceRemoteMatched.remoteCompilerMatches != null;

            Gson gson = JsonSerializationVisitor.createDebugSerializer().getGson();
            JsonObject object = new JsonObject();

            object.add("source", gson.toJsonTree(compiledText.sourceRemoteMatched.preprocessedSource.sourceCompilerSettings.sourceSettingsConnected.sourceSettings.selectedSourceSettings));

            JsonObject matchObject = new JsonObject();
            matchObject.addProperty("cached", compiledText.sourceRemoteMatched.cached);
            matchObject.add("chosenMatch", gson.toJsonTree(compiledText.sourceRemoteMatched.remoteCompilerMatches.getChosenMatch()));
            object.add("matchToRemoteCompiler", matchObject);

            String text = gson.toJson(object);
            textConsumer.apply(text);
        }
    }
}
