package com.compilerexplorer.datamodel.json;

import com.compilerexplorer.datamodel.ProjectSources;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;

public class ProjectSettingsSerializer implements JsonSerializer<ProjectSources> {
    @NotNull
    public static final ProjectSettingsSerializer INSTANCE = new ProjectSettingsSerializer();

    @Override
    public JsonElement serialize(ProjectSources projectSources, Type type, JsonSerializationContext context) {
        JsonObject object = new JsonObject();
        object.add("settings", context.serialize(projectSources.getSources()));
        return object;
    }
}
