package com.compilerexplorer.common;

import org.jetbrains.annotations.NotNull;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ActionListenerIgnoringEvent implements ActionListener {
    @NotNull
    private final Runnable runnable;

    public ActionListenerIgnoringEvent(@NotNull Runnable runnable_) {
        runnable = runnable_;
    }

    public void actionPerformed(ActionEvent e) {
        runnable.run();
    }
}
