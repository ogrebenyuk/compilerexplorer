package com.compilerexplorer.explorer;

import com.compilerexplorer.common.Bundle;
import com.compilerexplorer.common.ExplorerUtil;
import com.compilerexplorer.common.TaskRunner;
import com.compilerexplorer.common.component.BaseRefreshableComponent;
import com.compilerexplorer.common.component.CEComponent;
import com.compilerexplorer.common.component.DataHolder;
import com.compilerexplorer.datamodel.RemoteCompilersOutput;
import com.compilerexplorer.datamodel.state.RemoteCompilerInfo;
import com.compilerexplorer.datamodel.state.SettingsState;
import com.compilerexplorer.explorer.common.Reader;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class RemoteCompilersProducer extends BaseRefreshableComponent {
    @NonNls
    private static final Logger LOG = Logger.getInstance(RemoteCompilersProducer.class);

    @NotNull
    private final Project project;
    @NotNull
    private final SettingsState state;
    @NotNull
    private final Consumer<String> successfulUrlConsumer;
    @NotNull
    private final TaskRunner taskRunner;

    public RemoteCompilersProducer(@NotNull Project project_,
                                   @NotNull SettingsState state_,
                                   @NotNull Consumer<String> successfulUrlConsumer_,
                                   @NotNull TaskRunner taskRunner_) {
        super();
        LOG.debug("created without next");

        project = project_;
        state = state_;
        successfulUrlConsumer = successfulUrlConsumer_;
        taskRunner = taskRunner_;
    }

    public RemoteCompilersProducer(@NotNull CEComponent nextComponent,
                                   @NotNull Project project_,
                                   @NotNull SettingsState state_,
                                   @NotNull Consumer<String> successfulUrlConsumer_,
                                   @NotNull TaskRunner taskRunner_) {
        super(nextComponent);
        LOG.debug("created");

        project = project_;
        state = state_;
        successfulUrlConsumer = successfulUrlConsumer_;
        taskRunner = taskRunner_;
    }

    public void testConnection(@NotNull Consumer<Exception> errorConsumer) {
        tryConnect(null, errorConsumer);
    }

    @Override
    protected void doClear(@NotNull DataHolder data) {
        LOG.debug("doClear");
        data.remove(RemoteCompilersOutput.KEY);
    }

    @Override
    protected void doReset() {
        LOG.debug("doReset");
        state.setConnected(false);
        state.clearRemoteCompilers();
    }

    @Override
    protected void doRefresh(@NotNull DataHolder data) {
        LOG.debug("doRefresh");
        tryConnect(data, null);
    }

    @Override
    protected void refreshNext(@NotNull DataHolder data) {
        // empty
    }

    private void tryConnect(@Nullable DataHolder data, @Nullable Consumer<Exception> errorConsumer) {
        String url = state.getUrl();
        String endpoint = url + ExplorerUtil.COMPILERS_ENDPOINT;
        if (!state.getConnected()) {
            taskRunner.runTask(new Task.Backgroundable(project, Bundle.format("compilerexplorer.RemoteCompilersProducer.TaskTitle", "Url", url)) {
                @Override
                public void run(@NotNull ProgressIndicator indicator) {
                    boolean canceled = false;
                    String output = null;
                    Exception encounteredException = null;
                    try {
                        List<RemoteCompilerInfo> compilers = new ArrayList<>();
                        output = Reader.readObjects(endpoint, indicator, RemoteCompilerInfo.class, (info, rawData) -> {info.setRawData(rawData); compilers.add(info);});
                        LOG.debug("found " + compilers.size() + " remote compilers");
                        state.setRemoteCompilers(compilers);
                        state.setConnected(true);
                    } catch (ProcessCanceledException canceledException) {
                        LOG.debug("canceled");
                        canceled = true;
                    } catch (Exception exception) {
                        LOG.debug("exception " + exception);
                        encounteredException = exception;
                    }
                    if (encounteredException == null) {
                        successfulUrlConsumer.accept(url);
                    } else if (errorConsumer != null) {
                        errorConsumer.accept(encounteredException);
                    }
                    if (data != null) {
                        data.put(RemoteCompilersOutput.KEY, new RemoteCompilersOutput(endpoint, false, canceled, new RemoteCompilersOutput.Output(output, encounteredException)));
                        RemoteCompilersProducer.super.refreshNext(data);
                    }
                }
            });
        } else {
            if (data != null) {
                LOG.debug("will not run because already connected");
                data.put(RemoteCompilersOutput.KEY, new RemoteCompilersOutput(endpoint, true, false, null));
                RemoteCompilersProducer.super.refreshNext(data);
            }
        }
    }
}
