package com.compilerexplorer.gui.tabs;

import com.compilerexplorer.common.Tabs;
import com.compilerexplorer.common.component.DataHolder;
import com.compilerexplorer.datamodel.state.SettingsState;
import com.compilerexplorer.gui.json.JsonSerializer;
import com.compilerexplorer.datamodel.state.RemoteCompilerInfo;
import com.google.gson.*;
import org.jetbrains.annotations.NotNull;

public class ExplorerSiteInfoTabProvider extends BaseExplorerSiteUtilProvider {
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
                JsonArray array = new JsonArray();
                for (RemoteCompilerInfo info : getState().getRemoteCompilers()) {
                    JsonObject element = gson.toJsonTree(info).getAsJsonObject();
                    element.remove(RemoteCompilerInfo.RAW_DATA_FIELD);
                    element.add(RemoteCompilerInfo.RAW_DATA_FIELD, JsonParser.parseString(info.getRawData()));
                    array.add(element);
                }
                return gson.toJson(array);
            }, contentConsumer);
        } else {
            showError(true, data, contentConsumer);
        }
    }
}
