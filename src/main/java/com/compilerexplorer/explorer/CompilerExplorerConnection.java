package com.compilerexplorer.explorer;

import com.compilerexplorer.common.CompilerExplorerConnectionConsumer;
import com.compilerexplorer.common.CompilerExplorerState;
import com.google.gson.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.http.HttpResponse;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.client.methods.HttpGet;

public class CompilerExplorerConnection {
    public static void connect(@NotNull Project project, @NotNull CompilerExplorerState state) {
        connect(project, state, true);
    }

    public static void tryConnect(@NotNull Project project, @NotNull CompilerExplorerState state) {
        connect(project, state, false);
    }

    private static class CompilerId {
        String id;
        String name;
        String lang;
    }

    private static void connect(@NotNull Project project, @NotNull CompilerExplorerState state, boolean publish) {
        CompilerExplorerState tmpState = new CompilerExplorerState();
        tmpState.copyFrom(state);
        Task.Backgroundable task = new Task.Backgroundable(project, "Connecting to Compiler Explorer instance " + state.getUrl()) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                tmpState.setConnected(false);
                tmpState.setLastConnectionStatus("");
                String url = tmpState.getUrl() + "/api/compilers";
                try {
                    CloseableHttpClient httpClient = HttpClients.createDefault();
                    HttpGet getRequest = new HttpGet(url);
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

                    JsonArray array = new JsonParser().parse(output).getAsJsonArray();
                    Gson gson = new Gson();
                    List<CompilerExplorerState.CompilerInfo> compilers = new ArrayList<>();
                    for (JsonElement elem : array) {
                        CompilerId compilerId = gson.fromJson(elem, CompilerId.class);
                        CompilerExplorerState.CompilerInfo info = new CompilerExplorerState.CompilerInfo();
                        info.setId(compilerId.id);
                        info.setName(compilerId.name);
                        info.setLanguage(compilerId.lang);
                        compilers.add(info);
                    }
                    tmpState.setCompilers(compilers);
                    tmpState.setConnected(true);
                } catch (ProcessCanceledException canceledException) {
                    // empty
                } catch (Exception e) {
                    tmpState.setLastConnectionStatus("Exception reading from " + url + ": " + e.getMessage());
                }
                indicator.checkCanceled();
                ApplicationManager.getApplication().invokeLater(() -> {
                    state.copyFrom(tmpState);
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
