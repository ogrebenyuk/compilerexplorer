package com.compilerexplorer.explorer;

import com.compilerexplorer.common.Constants;
import com.compilerexplorer.common.RefreshSignal;
import com.compilerexplorer.common.RefreshableComponent;
import com.compilerexplorer.common.TaskRunner;
import com.compilerexplorer.datamodel.SelectedSourceSettings;
import com.compilerexplorer.datamodel.SourceSettingsConnected;
import com.compilerexplorer.datamodel.state.RemoteCompilerInfo;
import com.compilerexplorer.datamodel.state.SettingsState;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.intellij.openapi.application.ApplicationManager;
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

public class RemoteCompilersProducer extends RefreshableComponent<SelectedSourceSettings> {
    @NotNull
    private final Project project;
    @NotNull
    private final SettingsState state;
    @NotNull
    private final Consumer<SourceSettingsConnected> consumer;
    @NotNull
    private final Consumer<String> successfulUrlConsumer;
    @NotNull
    private final TaskRunner taskRunner;

    public RemoteCompilersProducer(@NotNull Project project_,
                                   @NotNull SettingsState state_,
                                   @NotNull Consumer<SourceSettingsConnected> consumer_,
                                   @NotNull Consumer<String> successfulUrlConsumer_,
                                   @NotNull TaskRunner taskRunner_) {
        project = project_;
        state = state_;
        consumer = consumer_;
        successfulUrlConsumer = successfulUrlConsumer_;
        taskRunner = taskRunner_;
    }

    public void testConnection(@NotNull Consumer<Exception> errorConsumer) {
        tryConnect(null, errorConsumer, true);
    }

    @Override
    public void accept(@NotNull SelectedSourceSettings sourceSettings) {
        super.accept(sourceSettings);

        if (!state.getEnabled()) {
            return;
        }

        tryConnect(sourceSettings, null, false);
    }

    private void tryConnect(@Nullable SelectedSourceSettings sourceSettings, @Nullable Consumer<Exception> errorConsumer, boolean force) {
        String url = state.getUrl();
        taskRunner.runTask(new Task.Backgroundable(project, Constants.PROJECT_TITLE + ": connecting to " + url) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                @Nullable SourceSettingsConnected sourceSettingsConnected = sourceSettings != null ? new SourceSettingsConnected(sourceSettings) : null;
                String endpoint = url + "/api/compilers";
                if (sourceSettingsConnected != null) {
                    sourceSettingsConnected.remoteCompilersEndpoint = endpoint;
                }
                boolean isValid = sourceSettings == null || sourceSettings.isValid();
                Exception[] encounteredExceptions = new Exception[]{null};
                if (isValid && (force || !state.getConnected())) {
                    if (sourceSettingsConnected != null) {
                        sourceSettingsConnected.remoteCompilersQueried = true;
                    }
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
                        String output = "";
                        String line;
                        while ((line = br.readLine()) != null) {
                            indicator.checkCanceled();
                            output = output.concat(line);
                        }
                        httpClient.close();
                        indicator.checkCanceled();

                        if (sourceSettingsConnected != null) {
                            sourceSettingsConnected.remoteCompilersRawOutput = output;
                        }
                        JsonArray array = JsonParser.parseString(output).getAsJsonArray();
                        Gson gson = new Gson();
                        List<RemoteCompilerInfo> compilers = new ArrayList<>();
                        for (JsonElement elem : array) {
                            RemoteCompilerInfo info = gson.fromJson(elem, RemoteCompilerInfo.class);
                            info.setRawData(elem.toString());
                            compilers.add(info);
                        }
                        indicator.checkCanceled();
                        state.setRemoteCompilers(compilers);
                        state.setConnected(true);
                    } catch (ProcessCanceledException canceledException) {
                        // empty
                    } catch (Exception exception) {
                        encounteredExceptions[0] = exception;
                        if (sourceSettingsConnected != null) {
                            sourceSettingsConnected.remoteCompilersException = exception;
                        }
                    }
                }
                ApplicationManager.getApplication().invokeLater(() -> {
                    if (encounteredExceptions[0] == null) {
                        successfulUrlConsumer.accept(url);
                    } else if (errorConsumer != null) {
                        errorConsumer.accept(encounteredExceptions[0]);
                    }
                    if (sourceSettingsConnected != null) {
                        consumer.accept(sourceSettingsConnected);
                    }
                });
            }
        });
    }

    @NotNull
    public Consumer<RefreshSignal> asReconnectSignalConsumer() {
        return refreshSignal -> {
            state.setConnected(SettingsState.EMPTY.getConnected());
            state.clearRemoteCompilers();
        };
    }
}
