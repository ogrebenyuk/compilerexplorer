package com.compilerexplorer.explorer;

import com.compilerexplorer.common.RemoteConnectionConsumer;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class RemoteConnectionListener {
    public RemoteConnectionListener(@NotNull Project project, @NotNull RemoteConnectionConsumer remoteConnectionConsumer) {
        project.getMessageBus().connect().subscribe(RemoteConnectionConsumer.TOPIC, remoteConnectionConsumer);
    }
}
