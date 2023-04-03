package com.compilerexplorer.gui.tabs;

import com.compilerexplorer.common.Tabs;
import com.compilerexplorer.common.component.DataHolder;
import com.compilerexplorer.gui.json.JsonSerializer;
import com.compilerexplorer.datamodel.state.RemoteCompilerInfo;
import com.google.gson.*;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class ExplorerSiteInfoTabProvider extends BaseExplorerSiteUtilProvider {
    public ExplorerSiteInfoTabProvider(@NotNull Project project) {
        super(project, Tabs.EXPLORER_SITE_INFO, "compilerexplorer.ShowExplorerSiteInfoTab");
    }

    @Override
    public boolean isSourceSpecific() {
        return false;
    }

    @Override
    public boolean isEnabled(@NotNull DataHolder data) {
        return !state.getConnected();
    }

    @Override
    public boolean isError(@NotNull DataHolder data) {
        return !state.getConnected();
    }

    @Override
    public void provide(@NotNull DataHolder data, @NotNull Consumer<String> textConsumer) {
        if (state.getConnected()) {
            Gson gson = JsonSerializer.createSerializer();
            JsonArray array = new JsonArray();
            for (RemoteCompilerInfo info : state.getRemoteCompilers()) {
                JsonObject element = gson.toJsonTree(info).getAsJsonObject();
                element.remove("rawData");
                element.add("rawData", JsonParser.parseString(info.getRawData()));
                array.add(element);
            }
            textConsumer.accept(gson.toJson(array));
        } else {
            showError(data, textConsumer);
        }
    }
}
