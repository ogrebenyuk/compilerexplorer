package com.compilerexplorer.common;

import com.intellij.openapi.application.ApplicationManager;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class LaterConsumerUnlessSuppressed<T> implements Consumer<T> {
    @NotNull
    private final Consumer<T> consumer;
    @NotNull
    private final SuppressionFlag flag;

    public LaterConsumerUnlessSuppressed(@NotNull Consumer<T> consumer_, @NotNull SuppressionFlag flag_) {
        consumer = consumer_;
        flag = flag_;
    }

    @Override
    public void accept(T t) {
        flag.unlessApplied(() -> ApplicationManager.getApplication().invokeLater(() -> consumer.accept(t)));
    }
}
