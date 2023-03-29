package com.compilerexplorer.datamodel.json;

import com.compilerexplorer.datamodel.SourceSettings;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;

public class SourceSettingsSerializer  implements JsonSerializer<SourceSettings> {
    @NotNull
    public static final SourceSettingsSerializer INSTANCE = new SourceSettingsSerializer();

    @Override
    public JsonElement serialize(SourceSettings sourceSettings, Type type, JsonSerializationContext context) {
        JsonObject object = new JsonObject();
        object.addProperty("sourcePath", sourceSettings.sourcePath);
        object.addProperty("sourceName", sourceSettings.sourceName);
        object.addProperty("language", sourceSettings.language);
        object.addProperty("languageSwitch", sourceSettings.languageSwitch);
        object.addProperty("compilerPath", sourceSettings.compilerPath);
        object.addProperty("compilerKind", sourceSettings.compilerKind);
        object.add("switches", context.serialize(sourceSettings.switches));
        object.addProperty("host", sourceSettings.host.getName() + (sourceSettings.host.isRemote() ? " " + sourceSettings.host.getHostId() : ""));
        return object;
    }
}
