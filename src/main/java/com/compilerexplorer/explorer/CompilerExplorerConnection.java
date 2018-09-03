package com.compilerexplorer.explorer;

import com.compilerexplorer.common.CompilerExplorerConnectionConsumer;
import com.compilerexplorer.common.CompilerExplorerState;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class CompilerExplorerConnection {
    public static void connect(@NotNull Project project, @NotNull CompilerExplorerState state) {
        connect(project, state, true);
    }

    public static void tryConnect(@NotNull Project project, @NotNull CompilerExplorerState state) {
        connect(project, state, false);
    }

    private static void connect(@NotNull Project project, @NotNull CompilerExplorerState state, boolean publish) {
        Task.Backgroundable task = new Task.Backgroundable(project, "Connecting to Compiler Explorer instance " + state.getUrl()) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                try {
                    Thread.sleep(5000);
                } catch (Exception e) {
                    // empty
                }
                indicator.checkCanceled();
                ApplicationManager.getApplication().invokeLater(() -> {
                    state.setConnected(true);
                    if (publish) {
                        publishConnection(project);
                    }
                });
            }
        };
        ProgressManager.getInstance().runProcessWithProgressAsynchronously(task, new BackgroundableProcessIndicator(task));
    }

    public static void publishConnectionLater(@NotNull Project project) {
        ApplicationManager.getApplication().invokeLater(() -> publishConnection(project));
    }

    private static void publishConnection(@NotNull Project project) {
        project.getMessageBus().syncPublisher(CompilerExplorerConnectionConsumer.TOPIC).connected();
    }
}
