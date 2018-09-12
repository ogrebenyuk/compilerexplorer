package com.compilerexplorer.project.clion.oc;

import com.intellij.openapi.project.Project;
import com.jetbrains.cidr.lang.workspace.OCWorkspaceModificationListener;
import org.jetbrains.annotations.NotNull;

import java.util.function.Consumer;

public class OCProjectListener {
    public OCProjectListener(@NotNull Project project, @NotNull Consumer<Boolean> changeConsumer) {
        project.getMessageBus().connect().subscribe(OCWorkspaceModificationListener.TOPIC, new OCWorkspaceModificationListener() {
            public void projectsChanged() {
                changeConsumer.accept(false);
            }
            public void projectFilesChanged() {
                changeConsumer.accept(false);
            }
            public void sourceFilesChanged() {
                changeConsumer.accept(false);
            }
            public void buildSettingsChanged() {
                changeConsumer.accept(false);
            }
            public void selectedResolveConfigurationChanged() {
                changeConsumer.accept(false);
            }
            public void buildFinished() {
                changeConsumer.accept(false);
            }
        });
    }
}
