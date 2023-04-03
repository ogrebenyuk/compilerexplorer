package com.compilerexplorer.project;

import com.compilerexplorer.common.component.BaseLinkedComponent;
import com.compilerexplorer.common.component.CEComponent;
import com.compilerexplorer.common.component.DataHolder;
import com.compilerexplorer.datamodel.ProjectSources;
import com.compilerexplorer.project.oc.OCProjectSettingsProducer;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.function.Supplier;

public class ProjectListener extends BaseLinkedComponent {
    private static final Logger LOG = Logger.getInstance(ProjectListener.class);

    @NotNull
    private final Supplier<ProjectSources> ocProjectSettingsProducer;

    public ProjectListener(@NotNull CEComponent nextComponent, @NotNull Project project) {
        super(nextComponent);
        LOG.debug("created");

        ocProjectSettingsProducer = new OCProjectSettingsProducer(project);
    }

    @Override
    public void refresh(@NotNull DataHolder data) {
        ProjectSources sources = ocProjectSettingsProducer.get();
        LOG.debug("refresh with " + sources.getSources().size() + " sources");
        super.refresh(data.with(ProjectSources.KEY, sources));
    }
}
