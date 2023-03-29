package com.compilerexplorer.explorer;

import com.compilerexplorer.common.*;
import com.compilerexplorer.datamodel.CompiledText;
import com.compilerexplorer.datamodel.SourceRemoteMatched;
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
import com.jetbrains.cidr.system.HostMachine;
import org.apache.http.Header;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.URI;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class RemoteCompiler extends RefreshableComponent<SourceRemoteMatched> {
    @NotNull
    private final Project project;
    @NotNull
    private final Consumer<CompiledText> compiledTextConsumer;
    @NotNull
    private final TaskRunner taskRunner;
    @NotNull
    private final Map<String, String> normalizedPathMap = new HashMap<>();

    public RemoteCompiler(@NotNull Project project_,
                          @NotNull Consumer<CompiledText> compiledTextConsumer_,
                          @NotNull TaskRunner taskRunner_) {
        project = project_;
        compiledTextConsumer = compiledTextConsumer_;
        taskRunner = taskRunner_;
    }

    @SuppressWarnings("WeakerAccess")
    @Override
    public void accept(@NotNull SourceRemoteMatched sourceRemoteMatched) {
        super.accept(sourceRemoteMatched);

        SettingsState state = CompilerExplorerSettingsProvider.getInstance(project).getState();

        if (!state.getEnabled()) {
            return;
        }

        SourceSettings sourceSettings = sourceRemoteMatched.preprocessedSource.sourceCompilerSettings.sourceSettingsConnected.sourceSettings.selectedSourceSettings;
        taskRunner.runTask(new Task.Backgroundable(project, Constants.PROJECT_TITLE + ": compiling " + (sourceSettings != null ? sourceSettings.sourceName : null)) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                CompiledText compiledText = new CompiledText(sourceRemoteMatched);
                if (sourceRemoteMatched.isValid() && sourceRemoteMatched.preprocessedSource.isValid() && sourceRemoteMatched.preprocessedSource.sourceCompilerSettings.sourceSettingsConnected.sourceSettings.isValid()) {
                    assert sourceSettings != null;
                    assert sourceRemoteMatched.remoteCompilerMatches != null;
                    assert sourceRemoteMatched.preprocessedSource.preprocessedText != null;
                    assert sourceRemoteMatched.preprocessedSource.sourceCompilerSettings.sourceSettingsConnected.sourceSettings.selectedSourceSettings != null;

                    String url = state.getUrl();
                    Filters filters = new Filters(state.getFilters());
                    String switches = getCompilerOptions(sourceSettings, state.getAdditionalSwitches(), state.getIgnoreSwitches());
                    String remoteCompilerId = sourceRemoteMatched.remoteCompilerMatches.getChosenMatch().getRemoteCompilerInfo().getId();
                    String endpoint = url + "/api/compiler/" + UrlEscapers.urlPathSegmentEscaper().escape(remoteCompilerId) + "/compile";
                    try {
                        CloseableHttpClient httpClient = HttpClients.createDefault();

                        HttpPost postRequest = new HttpPost(endpoint);
                        postRequest.addHeader("accept", "application/json");

                        Gson gson = new Gson();

                        Request request = new Request();
                        request.source = sourceRemoteMatched.preprocessedSource.preprocessedText;
                        request.options = new Options();
                        request.options.userArguments = switches;
                        request.options.filters = filters;
                        request.options.compilerOptions = new CompilerOptions();
                        request.options.compilerOptions.executorRequest = false;

                        String rawInput = gson.toJson(request);
                        compiledText.rawInput = rawInput;

                        postRequest.setEntity(new StringEntity(rawInput, ContentType.APPLICATION_JSON));

                        CloseableHttpResponse[] responses = {null};
                        Exception[] exceptions = {null};
                        Thread thread = new Thread(() -> {
                            try {
                                while (true) {
                                    responses[0] = httpClient.execute(postRequest);

                                    boolean isRedirected = responses[0].getStatusLine().getStatusCode() / 100 == 3;
                                    if (isRedirected) {
                                        Header[] headers = responses[0].getHeaders("Location");
                                        if (headers != null && headers.length > 0) {
                                            postRequest.setURI(new URI(headers[0].getValue()));
                                            continue;
                                        }
                                    }

                                    break;
                                }
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

                        String rawOutput = output.toString();
                        compiledText.rawOutput = rawOutput;

                        JsonObject obj = JsonParser.parseString(rawOutput).getAsJsonObject();
                        CompiledText.CompiledResult compiledResult = gson.fromJson(obj, CompiledText.CompiledResult.class);
                        compiledText.compiledResult = compiledResult;

                        if (compiledResult.code == 0) {
                            HostMachine host = sourceRemoteMatched.preprocessedSource.sourceCompilerSettings.sourceSettingsConnected.sourceSettings.selectedSourceSettings.host;
                            @Nullable File compilerDir = new File(sourceRemoteMatched.preprocessedSource.sourceCompilerSettings.sourceSettingsConnected.sourceSettings.selectedSourceSettings.compilerPath).getParentFile();
                            @Nullable String compilerInstallPath = compilerDir != null ? compilerDir.getParent() : null;
                            normalizePaths(compiledResult.stdout, host, compilerInstallPath);
                            normalizePaths(compiledResult.stderr, host, compilerInstallPath);
                            normalizePaths(compiledResult.asm, host, compilerInstallPath);
                            if (compiledResult.execResult != null) {
                                normalizePaths(compiledResult.execResult.stdout, host, compilerInstallPath);
                            }
                        }
                    } catch (ProcessCanceledException canceledException) {
                        // empty
                    } catch (Exception exception) {
                        compiledText.exception = exception;
                    }
                }

                ApplicationManager.getApplication().invokeLater(() -> compiledTextConsumer.accept(compiledText));
            }
        });
    }

    @NotNull
    private static String getCompilerOptions(@NotNull SourceSettings sourceSettings, @NotNull String additionalSwitches, @NotNull String ignoreSwitches) {
        List<String> ignoreSwitchesList = Arrays.asList(ignoreSwitches.split(" "));
        return sourceSettings.switches.stream().filter(x -> !ignoreSwitchesList.contains(x)).map(s -> "\"" + s + "\"").collect(Collectors.joining(" "))
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

    private void normalizePaths(@Nullable List<CompiledText.CompiledChunk> chunks, @NotNull HostMachine host, @Nullable String compilerInstallPath) {
        if (chunks != null) {
            chunks.forEach(chunk -> {
                if (chunk.source != null) {
                    chunk.source.file = tryNormalizePath(chunk.source.file, host, compilerInstallPath);
                }
            });
        }
    }

    @Nullable
    private String tryNormalizePath(@Nullable String path, @NotNull HostMachine host, @Nullable String compilerInstallPath) {
        if (path == null) {
            return null;
        }
        return normalizedPathMap.computeIfAbsent(path, p ->
                PathNormalizer.resolvePathFromCompilerHostToLocal(p, host, project.getBasePath(), compilerInstallPath)
        );
    }

    @NotNull
    public Consumer<RefreshSignal> asResetSignalConsumer() {
        return refreshSignal -> normalizedPathMap.clear();
    }
}
