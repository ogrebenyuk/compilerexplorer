package com.compilerexplorer.project;

import com.compilerexplorer.common.component.BaseLinkedComponent;
import com.compilerexplorer.common.component.CEComponent;
import com.compilerexplorer.common.component.DataHolder;
import com.compilerexplorer.datamodel.ProjectSources;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ProjectListener extends BaseLinkedComponent {
    @NonNls
    private static final Logger LOG = Logger.getInstance(ProjectListener.class);

    @NotNull
    private final Project project;

    public ProjectListener(@NotNull CEComponent nextComponent, @NotNull Project project_) {
        super(nextComponent);
        LOG.debug("created");
        project = project_;
    }

    @Override
    public void refresh(@NotNull DataHolder data) {
        ProjectSources sources = getSources(ProjectSettingsProducer.PROJECT_SETTINGS_PRODUCER_EP.getExtensionList());
        LOG.debug("refresh with " + sources.getSources().size() + " sources");
        super.refresh(data.with(ProjectSources.KEY, sources));
    }

    @NotNull
    private ProjectSources getSources(List<ProjectSettingsProducer> producers) {
        if (producers.size() == 1) {
            return producers.get(0).get(project);
        } else {
            return new ProjectSources(producers.stream().map(producer -> producer.get(project).getSources()).flatMap(List::stream).toList());
        }
    }
}
