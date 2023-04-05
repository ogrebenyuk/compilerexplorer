package com.compilerexplorer.gui.tabs;

import com.compilerexplorer.common.Bundle;
import com.compilerexplorer.common.Tabs;
import com.compilerexplorer.common.component.DataHolder;
import com.compilerexplorer.datamodel.SelectedSource;
import com.compilerexplorer.datamodel.SourceRemoteMatched;
import com.compilerexplorer.gui.json.JsonSerializer;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.intellij.json.JsonFileType;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class SourceInfoTabProvider extends BaseTabProvider {
    @NonNls
    @NotNull
    private static final String SOURCE_KEY = "source";
    @NonNls
    @NotNull
    private static final String MATCH_KEY = "matchToRemoteCompiler";

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
    public void provide(@NotNull DataHolder data, @NotNull Consumer<String> textConsumer) {
        data.get(SelectedSource.KEY).ifPresentOrElse(selectedSource -> {
            Gson gson = JsonSerializer.createSerializer();
            JsonObject object = new JsonObject();

            object.add(SOURCE_KEY, gson.toJsonTree(selectedSource));

            data.get(SourceRemoteMatched.SELECTED_KEY).ifPresent(sourceRemoteMatched -> object.add(MATCH_KEY, gson.toJsonTree(sourceRemoteMatched)));

            String text = gson.toJson(object);
            textConsumer.accept(text);
        }, () -> textConsumer.accept(Bundle.get("compilerexplorer.SourceInfoTabProvider.NoSelection")));
    }
}
