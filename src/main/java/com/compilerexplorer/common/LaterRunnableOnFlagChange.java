package com.compilerexplorer.common;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.util.Producer;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class LaterRunnableOnFlagChange implements Runnable {
    private boolean lastSeen;
    @NotNull
    private final Producer<Boolean> producer;
    @NotNull
    private final Consumer<Boolean> consumerOnChange;

    public LaterRunnableOnFlagChange(@NotNull Producer<Boolean> producer_, @NotNull Consumer<Boolean> consumerOnChange_) {
        producer = producer_;
        consumerOnChange = consumerOnChange_;

        lastSeen = producer_.produce();
    }

    @Override
    public void run() {
        ApplicationManager.getApplication().invokeLater(() -> {
            try {
                boolean flag = producer.produce();
                if (flag != lastSeen) {
                    lastSeen = flag;
                    consumerOnChange.accept(flag);
                }
            } catch (Exception e) {
                // empty
            }
        });
    }
}
