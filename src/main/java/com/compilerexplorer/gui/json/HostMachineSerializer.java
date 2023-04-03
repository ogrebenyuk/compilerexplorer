package com.compilerexplorer.gui.json;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.jetbrains.cidr.system.HostMachine;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Type;

public class HostMachineSerializer implements JsonSerializer<HostMachine> {
    @NotNull
    public static final HostMachineSerializer INSTANCE = new HostMachineSerializer();

    @Override
    public JsonElement serialize(HostMachine host, Type type, JsonSerializationContext context) {
        JsonObject object = new JsonObject();
        object.addProperty("name", host.getName());
        object.addProperty("isRemote", host.isRemote());
        object.addProperty("isWsl", host.isWsl());
        object.addProperty("hasRemoteFS", host.hasRemoteFS());
        object.add("OSType", context.serialize(host.getOSType()));
        object.addProperty("id", host.getHostId());
        return object;
    }
}
