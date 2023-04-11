package com.compilerexplorer.gui.tabs;

import com.compilerexplorer.common.Bundle;
import com.compilerexplorer.common.Tabs;
import com.compilerexplorer.common.component.DataHolder;
import com.compilerexplorer.datamodel.ProjectSources;
import com.compilerexplorer.datamodel.SelectedSource;
import com.compilerexplorer.datamodel.SourceRemoteMatched;
import com.compilerexplorer.datamodel.state.SettingsState;
import com.compilerexplorer.gui.json.JsonSerializer;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.intellij.json.JsonFileType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class SourceInfoTabProvider extends BaseTabProvider {
    @NonNls
    @NotNull
    private static final String SOURCE_KEY = "source";
    @NonNls
    @NotNull
    private static final String MATCH_KEY = "matchToRemoteCompiler";

    public SourceInfoTabProvider(@NotNull SettingsState state) {
        super(state, Tabs.SOURCE_INFO, "compilerexplorer.ShowSourceInfoTab", JsonFileType.INSTANCE);
    }

    @Override
    public void provide(@NotNull DataHolder data, @NotNull TabContentConsumer contentConsumer) {
        data.get(SelectedSource.KEY).ifPresentOrElse(
        selectedSource -> content(false, () -> {
            Gson gson = JsonSerializer.createSerializer();
            JsonObject object = new JsonObject();
            object.add(SOURCE_KEY, gson.toJsonTree(selectedSource));
            object.add(MATCH_KEY, gson.toJsonTree(data.get(SourceRemoteMatched.SELECTED_KEY).orElse(null)));
            return gson.toJson(object);
        }, contentConsumer),
        () -> {
            if (sourcesPresent(data)) {
                error(true, () -> Bundle.get("compilerexplorer.SourceInfoTabProvider.NoSelection"), contentConsumer);
            } else {
                message(() -> Bundle.get("compilerexplorer.SourceInfoTabProvider.NoSelection"), contentConsumer);
            }
        });
    }

    private static boolean sourcesPresent(@NotNull DataHolder data) {
        return data.get(ProjectSources.KEY).map(sources -> !sources.getSources().isEmpty()).orElse(false);
    }
}
