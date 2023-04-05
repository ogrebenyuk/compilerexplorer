package com.compilerexplorer.common;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.util.Producer;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class LaterRunnableOnFlagChange implements Runnable {
    @NonNls
    private static final Logger LOG = Logger.getInstance(LaterRunnableOnFlagChange.class);

    private boolean lastSeen;
    @NotNull
    private final Producer<Boolean> producer;
    @NotNull
    private final Consumer<Boolean> consumerOnChange;

    public LaterRunnableOnFlagChange(@NotNull Producer<Boolean> producer_, @NotNull Consumer<Boolean> consumerOnChange_) {
        LOG.debug("created");

        producer = producer_;
        consumerOnChange = consumerOnChange_;

        lastSeen = producer_.produce();
    }

    @Override
    public void run() {
        ApplicationManager.getApplication().invokeLater(() -> {
            try {
                boolean flag = producer.produce();
                boolean changed = flag != lastSeen;
                LOG.debug("flag " + lastSeen + " -> " + flag + ", changed " + changed);
                if (changed) {
                    lastSeen = flag;
                    consumerOnChange.accept(flag);
                }
            } catch (Exception e) {
                LOG.debug("exception " + e);
            }
        });
    }
}
