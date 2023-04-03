package com.compilerexplorer.gui.tabs;

import com.compilerexplorer.common.Tabs;
import com.compilerexplorer.common.component.DataHolder;
import com.compilerexplorer.gui.json.JsonSerializer;
import com.google.gson.*;
import com.intellij.json.JsonFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.util.keyFMap.KeyFMap;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class EverythingTabProvider extends BaseExplorerUtilProvider {
    public EverythingTabProvider(@NotNull Project project) {
        super(project, Tabs.EVERYTHING, "compilerexplorer.ShowEverythingTab", JsonFileType.INSTANCE);
    }

    @Override
    public boolean isSourceSpecific() {
        return false;
    }

    @Override
    public boolean isEnabled(@NotNull DataHolder data) {
        return false;
    }

    @Override
    public boolean isError(@NotNull DataHolder data) {
        return false;
    }

    @Override
    public void provide(@NotNull DataHolder data, @NotNull Consumer<String> textConsumer) {
        Gson gson = JsonSerializer.createSerializer();
        JsonObject everything = new JsonObject();
        everything.add("data", getData(gson, data));
        everything.add("state", gson.toJsonTree(getState()));
        textConsumer.accept(gson.toJson(everything));
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

    @NotNull
    private static String getKeyDisplayName(@NotNull String keyClassName) {
        return keyClassName.replaceFirst(".*\\.", "");
    }
}
