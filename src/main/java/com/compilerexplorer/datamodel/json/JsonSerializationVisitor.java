package com.compilerexplorer.datamodel.json;

import com.compilerexplorer.datamodel.ProjectSettings;
import com.compilerexplorer.datamodel.SourceSettings;
import com.compilerexplorer.datamodel.Visitable;
import com.compilerexplorer.datamodel.Visitor;
import com.google.gson.*;
import org.jetbrains.annotations.NotNull;

public class JsonSerializationVisitor implements Visitor {
    @NotNull
    private final Gson gson;
    @NotNull
    private String output = "";

    @NotNull
    public static JsonSerializationVisitor createDebugSerializer() {
        return new JsonSerializationVisitor(true, true, true);
    }

    private JsonSerializationVisitor(boolean prettyPrinting, boolean disableHtmlEscaping, boolean serializeNulls) {
        GsonBuilder builder = new GsonBuilder();
        if (prettyPrinting) {
            builder.setPrettyPrinting();
        }
        if (disableHtmlEscaping) {
            builder.disableHtmlEscaping();
        }
        if (serializeNulls) {
            builder.serializeNulls();
        }
        builder.registerTypeAdapter(SourceSettings.class, SourceSettingsSerializer.INSTANCE);
        builder.registerTypeAdapter(ProjectSettings.class, ProjectSettingsSerializer.INSTANCE);
        gson = builder.create();
    }

    @NotNull
    public Gson getGson() {
        return gson;
    }

    @NotNull
    public String output() {
        return output;
    }

    public void visit(@NotNull Visitable visitable) {
        if (visitable instanceof SourceSettings sourceSettings) {
            output = gson.toJson(sourceSettings);
        } else if (visitable instanceof ProjectSettings projectSettings) {
            output = gson.toJson(projectSettings);
        } else {
            unexpected(visitable);
        }
    }
}
