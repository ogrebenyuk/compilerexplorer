package com.compilerexplorer.common;

import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator;
import org.jetbrains.annotations.Nullable;

public class TaskRunner {
    @Nullable
    private BackgroundableProcessIndicator currentProgressIndicator;

    public void runTask(Task.Backgroundable task) {
        reset();
        currentProgressIndicator = new BackgroundableProcessIndicator(task);
        ProgressManager.getInstance().runProcessWithProgressAsynchronously(task, currentProgressIndicator);
    }

    public void reset() {
        if (currentProgressIndicator != null) {
            currentProgressIndicator.cancel();
        }
    }
}
