package com.compilerexplorer.explorer;

import com.compilerexplorer.common.Constants;
import com.compilerexplorer.common.TaskRunner;
import com.compilerexplorer.common.component.BaseRefreshableComponent;
import com.compilerexplorer.common.component.CEComponent;
import com.compilerexplorer.common.component.DataHolder;
import com.compilerexplorer.datamodel.RemoteCompilersOutput;
import com.compilerexplorer.datamodel.state.RemoteCompilerInfo;
import com.compilerexplorer.datamodel.state.SettingsState;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class RemoteCompilersProducer extends BaseRefreshableComponent {
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
    protected void doReset(@NotNull DataHolder data) {
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
        String endpoint = url + "/api/compilers";
        if (!state.getConnected()) {
            taskRunner.runTask(new Task.Backgroundable(project, Constants.PROJECT_TITLE + ": connecting to " + url) {
                @Override
                public void run(@NotNull ProgressIndicator indicator) {
                    boolean canceled = false;
                    String output = null;
                    Exception encounteredException = null;
                    try {
                        CloseableHttpClient httpClient = HttpClients.createDefault();
                        HttpGet getRequest = new HttpGet(endpoint);
                        getRequest.addHeader("accept", "application/json");
                        HttpResponse response = httpClient.execute(getRequest);
                        if (response.getStatusLine().getStatusCode() != 200) {
                            httpClient.close();
                            throw new RuntimeException("Failed : HTTP error code : " + response.getStatusLine().getStatusCode() + " from " + url);
                        }
                        BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                        StringBuilder outputBuilder = new StringBuilder();
                        String line;
                        while ((line = br.readLine()) != null) {
                            indicator.checkCanceled();
                            outputBuilder.append(line);
                        }
                        httpClient.close();
                        indicator.checkCanceled();

                        String rawOutput = outputBuilder.toString();
                        JsonArray array = JsonParser.parseString(rawOutput).getAsJsonArray();
                        Gson gson = new Gson();
                        List<RemoteCompilerInfo> compilers = new ArrayList<>();
                        for (JsonElement elem : array) {
                            RemoteCompilerInfo info = gson.fromJson(elem, RemoteCompilerInfo.class);
                            info.setRawData(elem.toString());
                            compilers.add(info);
                        }
                        indicator.checkCanceled();

                        LOG.debug("found " + compilers.size() + " remote compilers");
                        output = rawOutput;
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
