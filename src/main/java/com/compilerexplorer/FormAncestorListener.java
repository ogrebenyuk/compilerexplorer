package com.compilerexplorer;

import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

public class FormAncestorListener {
    private static class Listener implements AncestorListener {
        @NonNls
        private static final Logger LOG = Logger.getInstance(Listener.class);

        @NotNull
        private final Runnable consumer;

        public Listener(@NotNull Runnable consumer_) {
            LOG.debug("created");
            consumer = consumer_;
        }

        @Override
        public void ancestorAdded(AncestorEvent event) {
            LOG.debug("ancestorAdded");
            consumer.run();
        }
        @Override
        public void ancestorRemoved(AncestorEvent event) {
            LOG.debug("ancestorRemoved");
            consumer.run();
        }
        @Override
        public void ancestorMoved(AncestorEvent event) {
            LOG.debug("ancestorMoved");
        }
    }

    public FormAncestorListener(@NotNull JComponent component, @NotNull Runnable consumer) {
        component.addAncestorListener(new Listener(consumer));
    }
}
