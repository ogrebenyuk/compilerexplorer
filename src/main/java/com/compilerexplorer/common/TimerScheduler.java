package com.compilerexplorer.common;

import com.intellij.openapi.application.ApplicationManager;
import org.jetbrains.annotations.NotNull;

import java.util.Timer;
import java.util.TimerTask;

public class TimerScheduler {
    private static final long DELAY_MILLIS = 1000;

    @NotNull
    private Timer timer = new Timer();

    public void schedule(@NotNull Runnable runnable) {
        timer.cancel();
        timer = new Timer();
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                ApplicationManager.getApplication().invokeLater(runnable);
            }
        }, DELAY_MILLIS);
    }
}
