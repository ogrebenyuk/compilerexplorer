package com.compilerexplorer.explorer;

import com.compilerexplorer.common.*;
import com.compilerexplorer.common.datamodel.CompiledText;
import com.compilerexplorer.common.datamodel.PreprocessedSource;
import com.compilerexplorer.common.datamodel.SourceSettings;
import com.compilerexplorer.common.datamodel.state.Filters;
import com.compilerexplorer.common.datamodel.state.RemoteCompilerId;
import com.compilerexplorer.common.datamodel.state.SettingsState;
import com.google.common.net.UrlEscapers;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class RemoteCompiler implements Consumer<PreprocessedSource> {
    @NotNull
    private final Project project;
    @NotNull
    private final Consumer<CompiledText> compiledTextConsumer;
    @NotNull
    private final Consumer<Error> errorConsumer;
    @NotNull
    private final TaskRunner taskRunner;
    @Nullable
    private PreprocessedSource lastPreprocessedSource;

    public RemoteCompiler(@NotNull Project project_,
                          @NotNull Consumer<CompiledText> compiledTextConsumer_,
                          @NotNull Consumer<Error> errorConsumer_,
                          @NotNull TaskRunner taskRunner_) {
        project = project_;
        compiledTextConsumer = compiledTextConsumer_;
        errorConsumer = errorConsumer_;
        taskRunner = taskRunner_;
    }

    @Override
    public void accept(@NotNull PreprocessedSource preprocessedSource) {
        lastPreprocessedSource = preprocessedSource;
        SettingsState state = SettingsProvider.getInstance(project).getState();

        if (!state.getEnabled()) {
            return;
        }

        SourceSettings sourceSettings = preprocessedSource.getSourceRemoteMatched().getSourceCompilerSettings().getSourceSettings();
        String url = state.getUrl();
        Filters filters = new Filters(state.getFilters());
        String switches = getCompilerOptions(sourceSettings, state.getAdditionalSwitches());
        String name = sourceSettings.getSource().getName();
        taskRunner.runTask(new Task.Backgroundable(project, "Compiler Explorer: compiling " + name) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                String remoteCompilerId = preprocessedSource.getSourceRemoteMatched().getRemoteCompilerMatches().getChosenMatch().getRemoteCompilerInfo().getId();
                String endpoint = url + "/api/compiler/" + UrlEscapers.urlPathSegmentEscaper().escape(remoteCompilerId) + "/compile";
                try {
                    CloseableHttpClient httpClient = HttpClients.createDefault();

                    HttpPost postRequest = new HttpPost(endpoint);
                    postRequest.addHeader("accept", "application/json");

                    Gson gson = new Gson();

                    String source = preprocessedSource.getPreprocessedText();
                    Request request = new Request();
                    request.source = source;
                    request.options = new Options();
                    request.options.userArguments = switches;
                    request.options.filters = filters;

                    postRequest.setEntity(new StringEntity(gson.toJson(request), ContentType.APPLICATION_JSON));

                    HttpResponse response = httpClient.execute(postRequest);
                    if (response.getStatusLine().getStatusCode() != 200) {
                        httpClient.close();
                        throw new RuntimeException("Failed : HTTP error code : " + response.getStatusLine().getStatusCode() + " from " + url);
                    }
                    BufferedReader br = new BufferedReader(new InputStreamReader(response.getEntity().getContent()));
                    StringBuilder output = new StringBuilder();
                    String line;
                    while ((line = br.readLine()) != null) {
                        indicator.checkCanceled();
                        output.append(line);
                    }
                    httpClient.close();
                    indicator.checkCanceled();

                    JsonObject obj = new JsonParser().parse(output.toString()).getAsJsonObject();
                    CompiledText.CompiledResult compiledResult = gson.fromJson(obj, CompiledText.CompiledResult.class);

                    String err = compiledResult.stderr.stream().map(c -> c.text).filter(Objects::nonNull).collect(Collectors.joining("\n"));

                    if (compiledResult.code == 0) {
                        ApplicationManager.getApplication().invokeLater(() -> compiledTextConsumer.accept(new CompiledText(preprocessedSource, new RemoteCompilerId(remoteCompilerId), compiledResult)));
                    } else {
                        errorLater(err);
                    }
                } catch (ProcessCanceledException canceledException) {
                    errorLater("Canceled compiling " + name);
                } catch (Exception e) {
                    errorLater("Exception compiling " + name + ": " + e.getMessage());
                }
            }
        });
    }

    private void errorLater(@NotNull String text) {
        ApplicationManager.getApplication().invokeLater(() -> errorConsumer.accept(new Error(text)));
    }

    @NotNull
    private static String getCompilerOptions(@NotNull SourceSettings sourceSettings, @NotNull String additionalSwitches) {
        return sourceSettings.getSwitches().stream().map(s -> "\"" + s + "\"").collect(Collectors.joining(" "))
             + (additionalSwitches.isEmpty() ? "" : " " + additionalSwitches);
    }

    private static class Options {
        String userArguments;
        Filters filters;
    }

    private static class Request {
        String source;
        Options options;
    }

    public void refresh() {
        if (lastPreprocessedSource != null && SettingsProvider.getInstance(project).getState().getEnabled()) {
            accept(lastPreprocessedSource);
        }
    }
}
