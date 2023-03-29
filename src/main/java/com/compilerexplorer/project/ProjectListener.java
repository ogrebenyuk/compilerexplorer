package com.compilerexplorer.project;

import com.compilerexplorer.common.CompilerExplorerSettingsProvider;
import com.compilerexplorer.common.Refreshable;
import com.compilerexplorer.common.TimerScheduler;
import com.compilerexplorer.datamodel.ProjectSettings;
import com.compilerexplorer.project.clion.oc.OCProjectListener;
import com.compilerexplorer.project.clion.oc.OCProjectSettingsProducer;
import com.compilerexplorer.project.idea.IdeaProjectListener;
import com.intellij.openapi.project.Project;
import com.intellij.util.Producer;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;
import java.util.function.Supplier;

public class ProjectListener implements Refreshable {
    @NotNull
    private final Project project;
    @NotNull
    private final Consumer<ProjectSettings> projectSettingsConsumer;
    @NotNull
    private final Producer<Boolean> allowRefresh;
    @NotNull
    private final Supplier<ProjectSettings> ocProjectSettingsProducer;
    @NotNull
    private final TimerScheduler timerScheduler = new TimerScheduler();

    public ProjectListener(@NotNull Project project_, @NotNull Consumer<ProjectSettings> projectSettingsConsumer_, @NotNull Producer<Boolean> allowRefresh_) {
        project = project_;
        projectSettingsConsumer = projectSettingsConsumer_;
        allowRefresh = allowRefresh_;

        ocProjectSettingsProducer = new OCProjectSettingsProducer(project);
        new IdeaProjectListener(project, this::changed);
        new OCProjectListener(project, this::changed);
    }

    private void changed() {
        timerScheduler.schedule(this::refresh, CompilerExplorerSettingsProvider.getInstance(project).getState().getDelayMillis());
    }

    @Override
    public void refresh() {
        if (!CompilerExplorerSettingsProvider.getInstance(project).getState().getEnabled()) {
            return;
        }

        if (!allowRefresh.produce()) {
            return;
        }

        projectSettingsConsumer.accept(ocProjectSettingsProducer.get());
    }
}
