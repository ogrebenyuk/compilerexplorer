package com.compilerexplorer.explorer;

import com.compilerexplorer.common.*;
import com.compilerexplorer.common.component.*;
import com.compilerexplorer.common.compilerkind.CompilerKind;
import com.compilerexplorer.common.compilerkind.CompilerKindFactory;
import com.compilerexplorer.datamodel.*;
import com.compilerexplorer.datamodel.state.EnabledRemoteLibraryInfo;
import com.compilerexplorer.datamodel.state.Filters;
import com.compilerexplorer.datamodel.state.SettingsState;
import com.compilerexplorer.explorer.common.Reader;
import com.google.common.collect.Streams;
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
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.net.URI;
import java.util.*;
import java.util.stream.Stream;

public class RemoteCompiler extends BaseRefreshableComponent {
    @NonNls
    private static final Logger LOG = Logger.getInstance(RemoteCompiler.class);
    @NonNls
    @NotNull
    private static final String LOCATION_HEADER = "Location";
    @NonNls
    @NotNull
    private static final String STDIN_FILENAME_MARKER = "<stdin>";

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

    @Override
    protected void doClear(@NotNull DataHolder data) {
        LOG.debug("doClear");
        data.remove(CompiledText.KEY);
    }

    @Override
    protected void doReset() {
        LOG.debug("doReset");
        normalizedPathMap.clear();
    }

    @Override
    protected void doRefresh(@NotNull DataHolder data) {
        LOG.debug("doRefresh");
        needRefreshNext = true;
        data.get(SelectedSource.KEY).ifPresentOrElse(selectedSource ->
        data.get(SourceRemoteMatched.SELECTED_KEY).ifPresentOrElse(selectedMatch ->
        data.get(PreprocessedSource.KEY).flatMap(PreprocessedSource::getPreprocessedText).ifPresentOrElse(preprocessedText -> {
            SettingsState state = CompilerExplorerSettingsProvider.getInstance(project).getState();
            needRefreshNext = false;
            taskRunner.runTask(new Task.Backgroundable(project, Bundle.format("compilerexplorer.RemoteCompiler.TaskTitle", "Source", selectedSource.getSelectedSource().sourceName)) {
                @Override
                public void run(@NotNull ProgressIndicator indicator) {
                    boolean canceled = false;
                    String rawInput = "";
                    String rawOutput = "";
                    Exception exception = null;
                    CompiledText.CompiledResult compiledResult = null;

                    String url = state.getUrl();
                    Filters filters = state.getFilters();
                    boolean preprocessedLocally = data.get(PreprocessedSource.KEY).map(PreprocessedSource::getPreprocessLocally).orElse(false);
                    @Nullable CompilerKind kind = CompilerKindFactory.findCompilerKind(selectedMatch.getMatches().getChosenMatch().getRemoteCompilerInfo().getCompilerType()).orElse(null);
                    if (kind == null) {
                        kind = CompilerKindFactory.findCompilerKind(selectedSource.getSelectedSource().compilerKind).orElse(null);
                    }
                    String switches = getCompilerOptions(selectedSource.getSelectedSource(), state.getAdditionalSwitches(), state.getIgnoreSwitches(), preprocessedLocally, kind);
                    String remoteCompilerId = selectedMatch.getMatches().getChosenMatch().getRemoteCompilerInfo().getId();
                    String endpoint = url + ExplorerUtil.COMPILER_API_ROOT + UrlEscapers.urlPathSegmentEscaper().escape(remoteCompilerId) + ExplorerUtil.COMPILE_ENDPOINT;
                    String compilerLanguage = selectedSource.getSelectedSource().language;
                    if (kind != null) {
                        compilerLanguage = kind.adjustSourceLanguage(compilerLanguage);
                    }
                    Optional<List<EnabledRemoteLibraryInfo>> enabledLibraries = state.getEnabledRemoteLibrariesForLanguage(compilerLanguage);
                    try {
                        CloseableHttpClient httpClient = HttpClients.createDefault();

                        HttpPost postRequest = new HttpPost(endpoint);
                        postRequest.addHeader(ExplorerUtil.ACCEPT_HEADER, ExplorerUtil.JSON_MIME_TYPE);

                        Gson gson = new Gson();

                        Request request = new Request();
                        request.source = preprocessedText;
                        request.options = new Options();
                        request.options.userArguments = switches;
                        request.options.filters = filters;
                        request.options.compilerOptions = new CompilerOptions();
                        request.options.compilerOptions.executorRequest = false;
                        request.options.libraries = enabledLibraries.orElse(new ArrayList<>());
                        request.lang = ExplorerUtil.language(compilerLanguage);

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
                                        Header[] headers = responses[0].getHeaders(LOCATION_HEADER);
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

                        rawOutput = Reader.read(endpoint, httpClient, response, indicator);

                        JsonObject obj = JsonParser.parseString(rawOutput).getAsJsonObject();
                        compiledResult = gson.fromJson(obj, CompiledText.CompiledResult.class);

                        if (compiledResult.code == 0) {
                            HostMachine host = selectedSource.getSelectedSource().host;
                            normalizePaths(compiledResult.stdout, host, compiledResult.inputFilename, selectedSource.getSelectedSource().sourcePath);
                            normalizePaths(compiledResult.stderr, host, compiledResult.inputFilename, selectedSource.getSelectedSource().sourcePath);
                            normalizePaths(compiledResult.asm, host, compiledResult.inputFilename, selectedSource.getSelectedSource().sourcePath);
                            if (compiledResult.execResult != null) {
                                normalizePaths(compiledResult.execResult.stdout, host, compiledResult.inputFilename, selectedSource.getSelectedSource().sourcePath);
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
    private static String getCompilerOptions(@NotNull SourceSettings selectedSource,
                                             @NotNull String additionalSwitches,
                                             @NotNull String ignoreSwitches,
                                             boolean preprocessedLocally,
                                             @Nullable CompilerKind kind) {
        List<String> ignoreSwitchesList = CommandLineUtil.parseCommandLine(ignoreSwitches);
        return CommandLineUtil.formCommandLine(Streams.concat(
                (kind != null ? kind.adjustSourceSwitches(selectedSource.switches) : selectedSource.switches).stream().filter(x -> !ignoreSwitchesList.contains(x)),
                (kind != null ? kind.additionalSwitches().stream() : Stream.empty()),
                (kind != null ? kind.additionalCompilerSwitches(preprocessedLocally).stream() : Stream.empty()),
                (!additionalSwitches.isEmpty() ? CommandLineUtil.parseCommandLine(additionalSwitches).stream().filter(x -> !ignoreSwitchesList.contains(x)) : Stream.empty())
        ).toList());
    }

    private static class CompilerOptions {
        boolean executorRequest;
    }

    private static class Options {
        String userArguments;
        Filters filters;
        CompilerOptions compilerOptions;
        List<EnabledRemoteLibraryInfo> libraries;
    }

    private static class Request {
        String source;
        Options options;
        String lang;
    }

    private void normalizePaths(@Nullable List<CompiledText.CompiledChunk> chunks,
                                @NotNull HostMachine host,
                                @NonNls @Nullable String remoteSourceFilename,
                                @NonNls @NotNull String localSourceFilename) {
        if (chunks != null) {
            chunks.forEach(chunk -> {
                if (chunk.source != null) {
                    if (chunk.source.file != null && (chunk.source.file.equals(remoteSourceFilename) || chunk.source.file.equals(STDIN_FILENAME_MARKER))) {
                        chunk.source.file = localSourceFilename;
                    }
                    chunk.source.file = tryNormalizePath(chunk.source.file, host);
                }
            });
        }
    }

    @Nullable
    private String tryNormalizePath(@Nullable String path, @NotNull HostMachine host) {
        if (path == null) {
            return null;
        }
        return normalizedPathMap.computeIfAbsent(path, p ->
                PathNormalizer.resolvePathFromCompilerHostToLocal(p, host, project.getBasePath())
        );
    }
}
