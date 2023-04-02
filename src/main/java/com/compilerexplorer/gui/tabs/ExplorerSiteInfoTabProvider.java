package com.compilerexplorer.gui.tabs;

import com.compilerexplorer.common.Tabs;
import com.compilerexplorer.common.component.DataHolder;
import com.compilerexplorer.datamodel.json.JsonSerializationVisitor;
import com.compilerexplorer.datamodel.state.RemoteCompilerInfo;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class ExplorerSiteInfoTabProvider extends BaseExplorerSiteUtilProvider {
    public ExplorerSiteInfoTabProvider(@NotNull Project project) {
        super(project, Tabs.EXPLORER_SITE_INFO, "compilerexplorer.ShowExplorerSiteInfoTab");
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
    public void provide(@NotNull DataHolder data, @NotNull Function<String, EditorEx> textConsumer) {
        if (state.getConnected()) {
            Gson gson = JsonSerializationVisitor.createDebugSerializer().getGson();
            JsonArray array = new JsonArray();
            for (RemoteCompilerInfo info : state.getRemoteCompilers()) {
                JsonElement element = JsonParser.parseString(info.getRawData());
                array.add(element);
            }
            String text = gson.toJson(array);
            textConsumer.apply(text);
        } else {
            showError(data, textConsumer);
        }
    }
}
