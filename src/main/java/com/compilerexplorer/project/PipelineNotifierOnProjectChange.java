package com.compilerexplorer.project;

import com.compilerexplorer.Pipeline;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public class PipelineNotifierOnProjectChange {
    @NotNull
    final Consumer<Pipeline> consumer;
    @Nullable
    final Key<Boolean> key;

    protected PipelineNotifierOnProjectChange(@NotNull Consumer<Pipeline> _consumer) {
        consumer = _consumer;
        key = null;
    }

    protected PipelineNotifierOnProjectChange(@NotNull Consumer<Pipeline> _consumer, @Nullable Key<Boolean> _key) {
        consumer = _consumer;
        key = _key;
    }

    protected void changed(@NotNull Project project) {
        if (key != null) {
            project.putUserData(key, true);
        }
        Pipeline pipeline = project.getUserData(Pipeline.KEY);
        if (pipeline != null) {
            consumer.accept(pipeline);
        }
    }
}
