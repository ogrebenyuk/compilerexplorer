package com.compilerexplorer.gui.json;

import com.google.gson.*;
import com.jetbrains.cidr.system.HostMachine;
import org.jetbrains.annotations.NotNull;

public class JsonSerializer {
    @NotNull
    public static Gson createSerializer() {
        GsonBuilder builder = new GsonBuilder();
        builder.setPrettyPrinting();
        builder.disableHtmlEscaping();
        builder.serializeNulls();
        builder.registerTypeAdapter(HostMachine.class, HostMachineSerializer.INSTANCE);
        return builder.create();
    }
}
