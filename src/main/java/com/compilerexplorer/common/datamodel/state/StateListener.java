package com.compilerexplorer.common.datamodel.state;

import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class StateListener {
    public StateListener(@NotNull Project project, @NotNull StateConsumer stateConsumer) {
        project.getMessageBus().connect().subscribe(StateConsumer.TOPIC, stateConsumer);
    }
}
