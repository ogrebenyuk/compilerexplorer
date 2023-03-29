package com.compilerexplorer.compiler;

import com.compilerexplorer.common.*;
import com.compilerexplorer.datamodel.SourceCompilerSettings;
import com.compilerexplorer.datamodel.SourceSettings;
import com.compilerexplorer.datamodel.SourceSettingsConnected;
import com.compilerexplorer.datamodel.state.LocalCompilerPath;
import com.compilerexplorer.datamodel.state.LocalCompilerSettings;
import com.compilerexplorer.datamodel.state.SettingsState;
import com.compilerexplorer.compiler.common.CompilerRunner;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class CompilerSettingsProducer implements Consumer<SourceSettingsConnected> {
    @NotNull
    private final Project project;
    @NotNull
    private final Consumer<SourceCompilerSettings> sourceCompilerSettingsConsumer;
    @NotNull
    private final TaskRunner taskRunner;

    public CompilerSettingsProducer(@NotNull Project project_,
                                    @NotNull Consumer<SourceCompilerSettings> sourceCompilerSettingsConsumer_,
                                    @NotNull TaskRunner taskRunner_) {
        project = project_;
        sourceCompilerSettingsConsumer = sourceCompilerSettingsConsumer_;
        taskRunner = taskRunner_;
    }

    @Override
    public void accept(@NotNull SourceSettingsConnected sourceSettingsConnected) {
        SettingsState state = CompilerExplorerSettingsProvider.getInstance(project).getState();

        if (!state.getEnabled()) {
            return;
        }

        taskRunner.runTask(new Task.Backgroundable(project, "Determining compiler version for "
                + (sourceSettingsConnected.sourceSettings.selectedSourceSettings != null ? sourceSettingsConnected.sourceSettings.selectedSourceSettings.sourceName : null)) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                SourceCompilerSettings sourceCompilerSettings = new SourceCompilerSettings(sourceSettingsConnected);
                SourceSettings sourceSettings = sourceSettingsConnected.sourceSettings.selectedSourceSettings;

                if (sourceSettingsConnected.isValid() && sourceSettingsConnected.sourceSettings.isValid()) {
                    assert(sourceSettings != null);

                    LocalCompilerSettings existingSettings = state.getLocalCompilerSettings().get(new LocalCompilerPath(sourceSettings.compilerPath));
                    if (existingSettings != null) {
                        sourceCompilerSettings.localCompilerSettings = existingSettings;
                    } else {
                        if (isSupportedCompilerType(sourceSettings.compilerKind)) {
                            sourceCompilerSettings.isSupportedCompilerType = true;
                            String[] versionCommandLine = getVersionCommandLine(sourceSettings);
                            sourceCompilerSettings.versionerCommandLine = versionCommandLine;
                            try {
                                File versionerWorkingDir =  sourceSettings.compilerWorkingDir;
                                sourceCompilerSettings.versionerWorkingDir = versionerWorkingDir;
                                CompilerRunner versionRunner = new CompilerRunner(sourceSettings.host, versionCommandLine, versionerWorkingDir, "", indicator, state.getCompilerTimeoutMillis());
                                sourceCompilerSettings.versionerExitCode = versionRunner.getExitCode();
                                sourceCompilerSettings.versionerStdout = versionRunner.getStdout();
                                sourceCompilerSettings.versionerStderr = versionRunner.getStderr();

                                String versionText = versionRunner.getStderr();
                                if (versionRunner.getExitCode() == 0 && !versionText.isEmpty()) {
                                    String compilerVersion = parseCompilerVersion(sourceSettings.compilerKind, versionText);
                                    String compilerTarget = parseCompilerTarget(versionText);
                                    if (!compilerVersion.isEmpty() && !compilerTarget.isEmpty()) {
                                        sourceCompilerSettings.localCompilerSettings = new LocalCompilerSettings(sourceSettings.compilerKind, compilerVersion, compilerTarget);
                                    }
                                }
                            } catch (ProcessCanceledException canceledException) {
                                // empty
                            } catch (Exception exception) {
                                sourceCompilerSettings.versionerException = exception;
                            }
                        }
                    }
                }
                ApplicationManager.getApplication().invokeLater(() -> {
                    if (sourceCompilerSettings.localCompilerSettings != null) {
                        assert sourceSettings != null;
                        state.getLocalCompilerSettings().put(new LocalCompilerPath(sourceSettings.compilerPath), sourceCompilerSettings.localCompilerSettings);
                    }
                    sourceCompilerSettingsConsumer.accept(sourceCompilerSettings);
                });
            }
        });
    }

    @NotNull
    public Consumer<RefreshSignal> asResetSignalConsumer() {
        return refreshSignal -> {
            SettingsState state = CompilerExplorerSettingsProvider.getInstance(project).getState();
            state.setLocalCompilerSettings(SettingsState.EMPTY.getLocalCompilerSettings());
        };
    }

    @NotNull
    private static String[] getVersionCommandLine(@NotNull SourceSettings sourceSettings) {
        return Stream.of(
                sourceSettings.compilerPath,
                "-v"
        ).toArray(String[]::new);
    }

    private static boolean isSupportedCompilerType(@NotNull String compilerKind) {
        return compilerKind.equals("GCC") || compilerKind.equals("Clang");
    }

    @NotNull
    private static String parseCompilerVersion(@NotNull String compilerKind, @NotNull String versionText) {
        return versionText.replace('\n', ' ').replaceAll(".*" + compilerKind.toLowerCase() + " version ([^ ]*).*", "$1");
    }

    @NotNull
    private static String parseCompilerTarget(@NotNull String versionText) {
        return versionText.replace('\n', ' ').replaceAll(".*Target: ([^-]*).*", "$1");
    }
}
