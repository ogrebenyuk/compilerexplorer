package com.compilerexplorer.explorer;

import com.compilerexplorer.common.*;
import com.compilerexplorer.common.component.*;
import com.compilerexplorer.datamodel.*;
import com.compilerexplorer.datamodel.state.Filters;
import com.compilerexplorer.datamodel.state.SettingsState;
import com.google.common.net.UrlEscapers;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.intellij.openapi.diagnostic.Logger;
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
import java.util.stream.Collectors;

public class RemoteCompiler extends BaseRefreshableComponent {
    private static final Logger LOG = Logger.getInstance(RemoteCompiler.class);

    @NotNull
    private final Project project;
    @NotNull
    private final TaskRunner taskRunner;
    @NotNull
    private final Map<String, String> normalizedPathMap = new HashMap<>();
    boolean needRefreshNext;

    public RemoteCompiler(@NotNull CEComponent nextComponent, @NotNull Project project_, @NotNull TaskRunner taskRunner_) {
        super(nextComponent);
        LOG.debug("created");

        project = project_;
        taskRunner = taskRunner_;
    }

    protected void doClear(@NotNull DataHolder data) {
        LOG.debug("doClear");
        data.remove(CompiledText.KEY);
    }

    protected void doReset(@NotNull DataHolder data) {
        LOG.debug("doReset");
        normalizedPathMap.clear();
    }

    protected void doRefresh(@NotNull DataHolder data) {
        LOG.debug("doRefresh");
        needRefreshNext = true;
        data.get(SelectedSource.KEY).ifPresentOrElse(selectedSource ->
        data.get(SourceRemoteMatched.SELECTED_KEY).ifPresentOrElse(selectedMatch ->
        data.get(PreprocessedSource.KEY).flatMap(PreprocessedSource::getPreprocessedText).ifPresentOrElse(preprocessedText -> {
            SettingsState state = CompilerExplorerSettingsProvider.getInstance(project).getState();
            needRefreshNext = false;
            taskRunner.runTask(new Task.Backgroundable(project, Constants.PROJECT_TITLE + ": compiling " + selectedSource.getSelectedSource().sourceName) {
                @Override
                public void run(@NotNull ProgressIndicator indicator) {
                    boolean canceled = false;
                    String rawInput = "";
                    String rawOutput = "";
                    Exception exception = null;
                    CompiledText.CompiledResult compiledResult = null;

                    String url = state.getUrl();
                    Filters filters = state.getFilters();
                    String switches = getCompilerOptions(selectedSource.getSelectedSource(), state.getAdditionalSwitches(), state.getIgnoreSwitches());
                    String remoteCompilerId = selectedMatch.getMatches().getChosenMatch().getRemoteCompilerInfo().getId();
                    String endpoint = url + "/api/compiler/" + UrlEscapers.urlPathSegmentEscaper().escape(remoteCompilerId) + "/compile";
                    try {
                        CloseableHttpClient httpClient = HttpClients.createDefault();

                        HttpPost postRequest = new HttpPost(endpoint);
                        postRequest.addHeader("accept", "application/json");

                        Gson gson = new Gson();

                        Request request = new Request();
                        request.source = preprocessedText;
                        request.options = new Options();
                        request.options.userArguments = switches;
                        request.options.filters = filters;
                        request.options.compilerOptions = new CompilerOptions();
                        request.options.compilerOptions.executorRequest = false;

                        rawInput = gson.toJson(request);

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
                            } catch (Exception exception_) {
                                exceptions[0] = exception_;
                            }
                        });
                        thread.start();

                        while (thread.isAlive()) {
                            thread.join(100);
                            try {
                                indicator.checkCanceled();
                            } catch (Exception exception_) {
                                thread.interrupt();
                                throw exception_;
                            }
                        }

                        if (exceptions[0] != null) {
                            throw exceptions[0];
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

                        rawOutput = output.toString();

                        JsonObject obj = JsonParser.parseString(rawOutput).getAsJsonObject();
                        compiledResult = gson.fromJson(obj, CompiledText.CompiledResult.class);

                        if (compiledResult.code == 0) {
                            HostMachine host = selectedSource.getSelectedSource().host;
                            @Nullable File compilerDir = new File(selectedSource.getSelectedSource().compilerPath).getParentFile();
                            @Nullable String compilerInstallPath = compilerDir != null ? compilerDir.getParent() : null;
                            normalizePaths(compiledResult.stdout, host, compilerInstallPath);
                            normalizePaths(compiledResult.stderr, host, compilerInstallPath);
                            normalizePaths(compiledResult.asm, host, compilerInstallPath);
                            if (compiledResult.execResult != null) {
                                normalizePaths(compiledResult.execResult.stdout, host, compilerInstallPath);
                            }
                            LOG.debug("compiled");
                        } else {
                            LOG.debug("bad code " + compiledResult.code);
                        }
                    } catch (ProcessCanceledException canceledException) {
                        LOG.debug("canceled");
                        canceled = true;
                    } catch (Exception exception_) {
                        LOG.debug("exception " + exception_);
                        exception = exception_;
                    }

                    data.put(CompiledText.KEY, new CompiledText(canceled, rawInput, rawOutput, exception, compiledResult));
                    RemoteCompiler.super.refreshNext(data);
                }
            });
        },
        () -> LOG.debug("cannot find input: preprocessed text")),
        () -> LOG.debug("cannot find input: match")),
        () -> LOG.debug("cannot find input: source"));
        if (needRefreshNext) {
            RemoteCompiler.super.refreshNext(data);
        }
    }

    @Override
    protected void refreshNext(@NotNull DataHolder data) {
        // empty
    }

    @NotNull
    private static String getCompilerOptions(@NotNull SourceSettings selectedSource, @NotNull String additionalSwitches, @NotNull String ignoreSwitches) {
        List<String> ignoreSwitchesList = Arrays.asList(ignoreSwitches.split(" "));
        return selectedSource.switches.stream().filter(x -> !ignoreSwitchesList.contains(x)).map(s -> "\"" + s + "\"").collect(Collectors.joining(" "))
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
}
