package com.compilerexplorer.explorer;

import com.compilerexplorer.common.Bundle;
import com.compilerexplorer.common.ExplorerUtil;
import com.compilerexplorer.common.TaskRunner;
import com.compilerexplorer.common.component.BaseRefreshableComponent;
import com.compilerexplorer.common.component.CEComponent;
import com.compilerexplorer.common.component.DataHolder;
import com.compilerexplorer.datamodel.ProjectSources;
import com.compilerexplorer.datamodel.RemoteCompilersOutput;
import com.compilerexplorer.datamodel.state.RemoteLibraryInfo;
import com.compilerexplorer.datamodel.state.SettingsState;
import com.compilerexplorer.explorer.common.Reader;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class RemoteLibrariesProducer extends BaseRefreshableComponent {
    @NonNls
    private static final Logger LOG = Logger.getInstance(RemoteLibrariesProducer.class);

    @NotNull
    private final Project project;
    @NotNull
    private final SettingsState state;
    @NotNull
    private final TaskRunner taskRunner;

    public RemoteLibrariesProducer(@NotNull CEComponent nextComponent,
                                   @NotNull Project project_,
                                   @NotNull SettingsState state_,
                                   @NotNull TaskRunner taskRunner_) {
        super(nextComponent);
        LOG.debug("created");

        project = project_;
        state = state_;
        taskRunner = taskRunner_;
    }

    @Override
    protected void doClear(@NotNull DataHolder data) {
        LOG.debug("doClear");
        data.remove(RemoteCompilersOutput.LIBRARIES_KEY);
    }

    @Override
    protected void doReset() {
        LOG.debug("doReset");
        state.clearRemoteLibraries();
    }

    @Override
    protected void doRefresh(@NotNull DataHolder data) {
        LOG.debug("doRefresh");
        tryConnect(data);
    }

    @Override
    protected void refreshNext(@NotNull DataHolder data) {
        // empty
    }

    private void tryConnect(@NotNull DataHolder data) {
        String url = state.getUrl();
        if (!state.getConnected()) {
            LOG.debug("will not run because not connected");
            RemoteLibrariesProducer.super.refreshNext(data);
        } else {
            data.get(ProjectSources.KEY).filter(projectSources -> !projectSources.getSources().isEmpty()).map(ProjectSources::getSources).ifPresentOrElse(
                sources -> {
                    Set<String> knownLanguages = state.getRemoteLibrariesLanguages();
                    List<String> projectLanguages = sources.stream().map(source -> source.language).distinct().toList();
                    List<String> missingLanguages = projectLanguages.stream().filter(language -> !knownLanguages.contains(language)).toList();
                    knownLanguages.stream().filter(language -> !projectLanguages.contains(language)).forEach(state::clearRemoteLibrariesForLanguage);
                    String endpointBase = url + ExplorerUtil.LIBRARIES_ENDPOINT;
                    if (missingLanguages.isEmpty()) {
                        LOG.debug("will not run because all languages are cached");
                        data.put(RemoteCompilersOutput.LIBRARIES_KEY, new RemoteCompilersOutput(endpointBase, true, false, null));
                        RemoteLibrariesProducer.super.refreshNext(data);
                    } else {
                        taskRunner.runTask(new Task.Backgroundable(project, Bundle.format("compilerexplorer.RemoteLibrariesProducer.TaskTitle", "Url", url, "Languages", String.join(" ", missingLanguages))) {
                            @Override
                            public void run(@NotNull ProgressIndicator indicator) {
                                boolean canceled = false;
                                StringBuilder outputBuilder = new StringBuilder();
                                Exception encounteredException = null;
                                try {
                                    for (String compilerLanguage : missingLanguages) {
                                        String endpoint = endpointBase + ExplorerUtil.language(compilerLanguage);
                                        List<RemoteLibraryInfo> libraries = new ArrayList<>();
                                        String output = Reader.readObjects(endpoint, indicator, RemoteLibraryInfo.class, (info, rawData) -> {info.setRawData(rawData); libraries.add(info);});
                                        outputBuilder.append(output);
                                        LOG.debug("found " + libraries.size() + " remote libraries for " + compilerLanguage);
                                        state.setRemoteLibrariesForLanguage(compilerLanguage, libraries);
                                    }
                                } catch (ProcessCanceledException canceledException) {
                                    LOG.debug("canceled");
                                    canceled = true;
                                } catch (Exception exception) {
                                    LOG.debug("exception " + exception);
                                    encounteredException = exception;
                                }
                                data.put(RemoteCompilersOutput.LIBRARIES_KEY, new RemoteCompilersOutput(endpointBase, false, canceled, new RemoteCompilersOutput.Output(outputBuilder.toString(), encounteredException)));
                                RemoteLibrariesProducer.super.refreshNext(data);
                            }
                        });
                    }
                },
                () -> {
                    LOG.debug("will not run because there are no sources");
                    RemoteLibrariesProducer.super.refreshNext(data);
                }
            );
        }
    }
}
