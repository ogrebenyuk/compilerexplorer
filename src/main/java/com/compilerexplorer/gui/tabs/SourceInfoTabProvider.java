package com.compilerexplorer.gui.tabs;

import com.compilerexplorer.common.Tabs;
import com.compilerexplorer.common.component.DataHolder;
import com.compilerexplorer.datamodel.SelectedSource;
import com.compilerexplorer.datamodel.SourceRemoteMatched;
import com.compilerexplorer.datamodel.json.JsonSerializationVisitor;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.intellij.json.JsonFileType;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class SourceInfoTabProvider extends BaseTabProvider {
    public SourceInfoTabProvider(@NotNull Project project) {
        super(project, Tabs.SOURCE_INFO, "compilerexplorer.ShowSourceInfoTab", JsonFileType.INSTANCE);
    }

    @Override
    public boolean isEnabled(@NotNull DataHolder data) {
        return false;
    }

    @Override
    public boolean isError(@NotNull DataHolder data) {
        return data.get(SelectedSource.KEY).isEmpty();
    }

    @Override
    public void provide(@NotNull DataHolder data, @NotNull Function<String, EditorEx> textConsumer) {
        data.get(SelectedSource.KEY).ifPresentOrElse(selectedSource -> {
            Gson gson = JsonSerializationVisitor.createDebugSerializer().getGson();
            JsonObject object = new JsonObject();

            object.add("source", gson.toJsonTree(selectedSource));

            data.get(SourceRemoteMatched.SELECTED_KEY).ifPresent(sourceRemoteMatched -> {
                JsonObject matchObject = new JsonObject();
                matchObject.addProperty("cached", sourceRemoteMatched.getCached());
                matchObject.add("chosenMatch", gson.toJsonTree(sourceRemoteMatched.getMatches().getChosenMatch()));
                object.add("matchToRemoteCompiler", matchObject);
            });

            String text = gson.toJson(object);
            textConsumer.apply(text);
        }, () -> textConsumer.apply("No source selected"));
    }
}
