import com.intellij.ProjectTopics;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.ModuleListener;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ModuleRootEvent;
import com.intellij.openapi.roots.ModuleRootListener;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.Function;
import org.fest.util.Lists;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

class CompilerExplorer {
    @NotNull private final Project project;

    CompilerExplorer(@NotNull Project project_) {
        project = project_;

        project.getMessageBus().connect().subscribe(ProjectTopics.PROJECT_ROOTS, new ModuleRootListener() {
            @Override
            public void beforeRootsChange(ModuleRootEvent event) {
                String projectName = project.getName();
                Messages.showInfoMessage("beforeRootsChange for " + projectName, "Project Properties");
            }

            @Override
            public void rootsChanged(ModuleRootEvent event) {
                String projectName = project.getName();
                Messages.showInfoMessage("rootsChanged for " + projectName, "Project Properties");
            }
        });
        project.getMessageBus().connect().subscribe(ProjectTopics.MODULES, new ModuleListener() {
            @Override
            public void moduleAdded(@NotNull Project project, @NotNull Module module) {
                String projectName = project.getName() + ":" + module.getName();
                Messages.showInfoMessage("moduleAdded for " + projectName, "Project Properties");
            }

            @Override
            public void beforeModuleRemoved(@NotNull Project project, @NotNull Module module) {
                String projectName = project.getName() + ":" + module.getName();
                Messages.showInfoMessage("beforeModuleRemoved for " + projectName, "Project Properties");
            }

            @Override
            public void moduleRemoved(@NotNull Project project, @NotNull Module module) {
                String projectName = project.getName() + ":" + module.getName();
                Messages.showInfoMessage("moduleRemoved for " + projectName, "Project Properties");
            }

            @Override
            public void modulesRenamed(@NotNull Project project, @NotNull List<Module> modules, @NotNull Function<Module, String> oldNameProvider) {
                String projectName = project.getName() + ":\n" + Arrays.stream(modules.toArray()).map(m -> {return oldNameProvider.fun((Module) m) + "->" + ((Module) m).getName();}).collect(Collectors.joining("\n"));
                Messages.showInfoMessage("modulesRenamed for " + projectName, "Project Properties");
            }
        });
    }

    void refresh() {
        String projectName = project.getName();
        VirtualFile[] vFiles = ProjectRootManager.getInstance(project).getContentSourceRoots();
        String sourceRootsList = Arrays.stream(vFiles).map(VirtualFile::getUrl).collect(Collectors.joining("\n"));

        Messages.showInfoMessage("Source roots for the " + projectName + " plugin:\n" + sourceRootsList, "Project Properties");
    }
}
