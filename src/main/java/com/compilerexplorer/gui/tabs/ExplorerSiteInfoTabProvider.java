package com.compilerexplorer.gui.tabs;

import com.compilerexplorer.common.Tabs;
import com.compilerexplorer.datamodel.CompiledText;
import com.compilerexplorer.datamodel.json.JsonSerializationVisitor;
import com.compilerexplorer.datamodel.state.RemoteCompilerInfo;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.intellij.json.JsonFileType;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class ExplorerSiteInfoTabProvider extends TabProvider {
    public ExplorerSiteInfoTabProvider(@NotNull Project project) {
        super(project, Tabs.EXPLORER_SITE_INFO, "compilerexplorer.ShowExplorerSiteInfoTab", JsonFileType.INSTANCE);
    }

    @Override
    public boolean isEnabled(@NotNull CompiledText compiledText) {
        return !state.getConnected();
    }

    @Override
    public boolean isError(@NotNull CompiledText compiledText) {
        return !state.getConnected();
    }

    @Override
    public void provide(@NotNull CompiledText compiledText, @NotNull Function<String, EditorEx> textConsumer) {
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
            if (compiledText.sourceRemoteMatched.preprocessedSource.sourceCompilerSettings.sourceSettingsConnected.remoteCompilersQueried) {
                if (compiledText.sourceRemoteMatched.preprocessedSource.sourceCompilerSettings.sourceSettingsConnected.remoteCompilersException != null) {
                    String errorMessage = "Error from Compiler Explorer endpoint \""
                            + compiledText.sourceRemoteMatched.preprocessedSource.sourceCompilerSettings.sourceSettingsConnected.remoteCompilersEndpoint
                            + "\":\n"
                            + compiledText.sourceRemoteMatched.preprocessedSource.sourceCompilerSettings.sourceSettingsConnected.remoteCompilersException;
                    textConsumer.apply(errorMessage);
                } else {
                    String errorMessage = "Unknown error from Compiler Explorer endpoint \""
                            + compiledText.sourceRemoteMatched.preprocessedSource.sourceCompilerSettings.sourceSettingsConnected.remoteCompilersEndpoint
                            + "\"";
                    textConsumer.apply(errorMessage);
                }
            } else {
                String errorMessage = "Compiler Explorer endpoint \""
                        + compiledText.sourceRemoteMatched.preprocessedSource.sourceCompilerSettings.sourceSettingsConnected.remoteCompilersEndpoint
                        + "\" was not queried";
                textConsumer.apply(errorMessage);
            }
        }
    }
}
