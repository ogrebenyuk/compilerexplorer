package com.compilerexplorer;

import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import javax.swing.event.AncestorEvent;
import javax.swing.event.AncestorListener;

public class FormAncestorListener {
    public FormAncestorListener(@NotNull JComponent component, @NotNull Runnable consumer) {
        component.addAncestorListener (new AncestorListener() {
            @Override
            public void ancestorAdded(AncestorEvent event) {
                consumer.run();
            }
            @Override
            public void ancestorRemoved(AncestorEvent event) {
                consumer.run();
            }
            @Override
            public void ancestorMoved(AncestorEvent event) {
                // empty
            }
        });
    }
}
