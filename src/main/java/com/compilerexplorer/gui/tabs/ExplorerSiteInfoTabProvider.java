package com.compilerexplorer.gui.tabs;

import com.compilerexplorer.common.Tabs;
import com.compilerexplorer.common.component.DataHolder;
import com.compilerexplorer.datamodel.state.RemoteLibraryInfo;
import com.compilerexplorer.datamodel.state.SettingsState;
import com.compilerexplorer.gui.json.JsonSerializer;
import com.compilerexplorer.datamodel.state.RemoteCompilerInfo;
import com.google.gson.*;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class ExplorerSiteInfoTabProvider extends BaseExplorerSiteUtilProvider {
    @NonNls
    @NotNull
    private static final String COMPILERS_KEY = "compilers";
    @NonNls
    @NotNull
    private static final String LIBRARIES_KEY = "libraries";
    @NonNls
    @NotNull
    private static final String LANGUAGE_KEY = "language";
    @NonNls
    @NotNull
    private static final String LANGUAGE_LIBRARIES_KEY = "librariesForLanguage";

    public ExplorerSiteInfoTabProvider(@NotNull SettingsState state) {
        super(state, Tabs.EXPLORER_SITE_INFO, "compilerexplorer.ShowExplorerSiteInfoTab");
    }

    @Override
    public boolean isSourceSpecific() {
        return false;
    }

    @Override
    public void provide(@NotNull DataHolder data, @NotNull TabContentConsumer contentConsumer) {
        if (getState().getConnected()) {
            content(false, () -> {
                Gson gson = JsonSerializer.createSerializer();
                JsonObject result = new JsonObject();

                JsonArray compilersArray = new JsonArray();
                for (RemoteCompilerInfo info : getState().getRemoteCompilers()) {
                    JsonObject element = gson.toJsonTree(info).getAsJsonObject();
                    element.remove(RemoteCompilerInfo.RAW_DATA_FIELD);
                    element.add(RemoteCompilerInfo.RAW_DATA_FIELD, JsonParser.parseString(info.getRawData()));
                    compilersArray.add(element);
                }
                result.add(COMPILERS_KEY, compilersArray);

                JsonArray librariesArray = new JsonArray();
                getState().getRemoteLibraries().forEach((language, libraries) -> {
                    JsonObject languageLibraries = new JsonObject();
                    languageLibraries.addProperty(LANGUAGE_KEY, language);
                    JsonArray languageLibrariesArray = new JsonArray();
                    libraries.forEach(library -> {
                        JsonObject element = gson.toJsonTree(library).getAsJsonObject();
                        element.remove(RemoteLibraryInfo.RAW_DATA_FIELD);
                        element.add(RemoteLibraryInfo.RAW_DATA_FIELD, JsonParser.parseString(library.getRawData()));
                        languageLibrariesArray.add(element);
                    });
                    languageLibraries.add(LANGUAGE_LIBRARIES_KEY, languageLibrariesArray);
                    librariesArray.add(languageLibraries);
                });
                result.add(LIBRARIES_KEY, librariesArray);

                return gson.toJson(result);
            }, contentConsumer);
        } else {
            showError(true, data, contentConsumer);
        }
    }
}
