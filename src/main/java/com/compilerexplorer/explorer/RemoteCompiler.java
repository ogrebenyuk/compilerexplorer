package com.compilerexplorer.explorer;

import com.compilerexplorer.common.*;
import com.compilerexplorer.datamodel.CompiledText;
import com.compilerexplorer.datamodel.PreprocessedSource;
import com.compilerexplorer.datamodel.SourceSettings;
import com.compilerexplorer.datamodel.state.Filters;
import com.compilerexplorer.datamodel.state.SettingsState;
import com.google.common.net.UrlEscapers;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;
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
    @NotNull
    private final Map<String, String> normalizedPathMap;

    public RemoteCompiler(@NotNull Project project_,
                          @NotNull Consumer<CompiledText> compiledTextConsumer_,
                          @NotNull Consumer<Error> errorConsumer_,
                          @NotNull TaskRunner taskRunner_) {
        project = project_;
        compiledTextConsumer = compiledTextConsumer_;
        errorConsumer = errorConsumer_;
        taskRunner = taskRunner_;
        normalizedPathMap = new HashMap<>();
    }

    @SuppressWarnings("WeakerAccess")
    @Override
    public void accept(@NotNull PreprocessedSource preprocessedSource) {
        lastPreprocessedSource = preprocessedSource;
        SettingsState state = CompilerExplorerSettingsProvider.getInstance(project).getState();

        if (!state.getEnabled()) {
            return;
        }

        SourceSettings sourceSettings = preprocessedSource.getSourceRemoteMatched().getSourceCompilerSettings().getSourceSettings();
        String url = state.getUrl();
        Filters filters = new Filters(state.getFilters());
        String switches = getCompilerOptions(sourceSettings, state.getAdditionalSwitches(), state.getIgnoreSwitches());
        String name = sourceSettings.getSourceName();
        taskRunner.runTask(new Task.Backgroundable(project, Constants.PROJECT_TITLE + ": compiling " + name) {
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
                    request.options.compilerOptions = new CompilerOptions();
                    request.options.compilerOptions.executorRequest = false;

                    postRequest.setEntity(new StringEntity(gson.toJson(request), ContentType.APPLICATION_JSON));

                    CloseableHttpResponse[] responses = {null};
                    Exception[] exceptions = {null};
                    Thread thread = new Thread(() -> {
                        try {
                            responses[0] = httpClient.execute(postRequest);
                        } catch (Exception exception) {
                            exceptions[0] = exception;
                        }
                    });
                    thread.start();

                    while (thread.isAlive()) {
                        thread.join(100);
                        try {
                            indicator.checkCanceled();
                        } catch (Exception exception) {
                            thread.interrupt();
                            throw exception;
                        }
                    }

                    Exception exception = exceptions[0];
                    if (exception != null) {
                        throw exception;
                    }

                    CloseableHttpResponse response = responses[0];

                    if (response.getStatusLine().getStatusCode() != 200) {
                        httpClient.close();
                        response.close();
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
                    response.close();
                    indicator.checkCanceled();

                    JsonObject obj = new JsonParser().parse(output.toString()).getAsJsonObject();
                    CompiledText.CompiledResult compiledResult = gson.fromJson(obj, CompiledText.CompiledResult.class);

                    if (compiledResult.code == 0) {
                        normalizePaths(compiledResult.stdout);
                        normalizePaths(compiledResult.stderr);
                        normalizePaths(compiledResult.asm);
                        ApplicationManager.getApplication().invokeLater(() -> compiledTextConsumer.accept(new CompiledText(preprocessedSource, compiledResult)));
                    } else {
                        String err = compiledResult.stderr.stream().map(c -> c.text).filter(Objects::nonNull).collect(Collectors.joining("\n"));
                        errorLater(err);
                    }
                } catch (ProcessCanceledException canceledException) {
                    //errorLater("Canceled compiling " + name);
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
    private static String getCompilerOptions(@NotNull SourceSettings sourceSettings, @NotNull String additionalSwitches, @NotNull String ignoreSwitches) {
        List<String> ignoreSwitchesList = Arrays.asList(ignoreSwitches.split(" "));
        return sourceSettings.getSwitches().stream().filter(x -> !ignoreSwitchesList.contains(x)).map(s -> "\"" + s + "\"").collect(Collectors.joining(" "))
                + (AdditionalSwitches.INSTANCE.isEmpty() ? "" : " " + String.join(" ", AdditionalSwitches.INSTANCE))
                + (additionalSwitches.isEmpty() ? "" : " " + Arrays.stream(additionalSwitches.split(" ")).filter(x -> !ignoreSwitchesList.contains(x)).collect(Collectors.joining(" ")));
    }

    private static class CompilerOptions {
        boolean executorRequest;
    }

    private static class Options {
        String userArguments;
        Filters filters;
        CompilerOptions compilerOptions;
    }

    private static class Request {
        String source;
        Options options;
    }

    public void refresh() {
        if (lastPreprocessedSource != null && CompilerExplorerSettingsProvider.getInstance(project).getState().getEnabled()) {
            accept(lastPreprocessedSource);
        }
    }

    private void normalizePaths(@NotNull List<CompiledText.CompiledChunk> chunks) {
        chunks.forEach(chunk -> {
            if (chunk.source != null) {
                chunk.source.file = tryNormalizePath(chunk.source.file);
            }
        });
    }

    @Nullable
    private String tryNormalizePath(@Nullable String path) {
        if (path == null) {
            return null;
        }
        return normalizedPathMap.computeIfAbsent(path, PathNormalizer::normalizePath);
    }

    @NotNull
    public Consumer<RefreshSignal> asResetSignalConsumer() {
        return refreshSignal -> normalizedPathMap.clear();
    }
}
