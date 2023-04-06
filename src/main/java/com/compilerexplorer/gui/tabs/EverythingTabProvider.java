package com.compilerexplorer.gui.tabs;

import com.compilerexplorer.common.Tabs;
import com.compilerexplorer.common.component.DataHolder;
import com.compilerexplorer.datamodel.state.SettingsState;
import com.compilerexplorer.gui.json.JsonSerializer;
import com.google.gson.*;
import com.intellij.json.JsonFileType;
import com.intellij.openapi.util.Key;
import com.intellij.util.keyFMap.KeyFMap;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class EverythingTabProvider extends BaseExplorerUtilProvider {
    @NonNls
    @NotNull
    private static final String DATA_KEY = "data";
    @NonNls
    @NotNull
    private static final String STATE_KEY = "state";

    public EverythingTabProvider(@NotNull SettingsState state) {
        super(state, Tabs.EVERYTHING, "compilerexplorer.ShowEverythingTab", JsonFileType.INSTANCE);
    }

    @Override
    public boolean isSourceSpecific() {
        return false;
    }

    @Override
    public void provide(@NotNull DataHolder data, @NotNull TabContentConsumer contentConsumer) {
        content(false, () -> {
            Gson gson = JsonSerializer.createSerializer();
            JsonObject everything = new JsonObject();
            everything.add(DATA_KEY, getData(gson, data));
            everything.add(STATE_KEY, gson.toJsonTree(getState()));
            return gson.toJson(everything);
        }, contentConsumer);
    }

    @NotNull
    private static JsonObject getData(@NotNull Gson gson, @NotNull DataHolder data) {
        JsonObject dataObj = new JsonObject();
        KeyFMap everything = data.getMap();
        for (Key<?> key : everything.getKeys()) {
            Object obj = everything.get(key);
            dataObj.add(getKeyDisplayName(key.toString()), gson.toJsonTree(obj));
        }
        return dataObj;
    }

    @NonNls
    @NotNull
    private static String getKeyDisplayName(@NonNls @NotNull String keyClassName) {
        return keyClassName.replaceFirst(".*\\.", "");
    }
}
