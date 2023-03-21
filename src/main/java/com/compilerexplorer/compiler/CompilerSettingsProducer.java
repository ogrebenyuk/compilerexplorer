package com.compilerexplorer.compiler;

import com.compilerexplorer.common.*;
import com.compilerexplorer.datamodel.SourceCompilerSettings;
import com.compilerexplorer.datamodel.SourceSettings;
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

import java.util.function.Consumer;
import java.util.stream.Stream;

public class CompilerSettingsProducer implements Consumer<SourceSettings> {
    @NotNull
    private final Project project;
    @NotNull
    private final Consumer<SourceCompilerSettings> sourceCompilerSettingsConsumer;
    @NotNull
    private final Consumer<Error> errorConsumer;
    @NotNull
    private final TaskRunner taskRunner;

    public CompilerSettingsProducer(@NotNull Project project_,
                                    @NotNull Consumer<SourceCompilerSettings> sourceCompilerSettingsConsumer_,
                                    @NotNull Consumer<Error> errorConsumer_,
                                    @NotNull TaskRunner taskRunner_) {
        project = project_;
        sourceCompilerSettingsConsumer = sourceCompilerSettingsConsumer_;
        errorConsumer = errorConsumer_;
        taskRunner = taskRunner_;
    }

    @Override
    public void accept(@NotNull SourceSettings sourceSettings) {
        SettingsState state = CompilerExplorerSettingsProvider.getInstance(project).getState();

        if (!state.getEnabled()) {
            return;
        }

        {
            LocalCompilerSettings existingSettings = state.getLocalCompilerSettings().get(new LocalCompilerPath(sourceSettings.getCompilerPath()));
            if (existingSettings != null) {
                sourceCompilerSettingsConsumer.accept(new SourceCompilerSettings(sourceSettings, existingSettings));
                return;
            }
        }

        if (!isSupportedCompilerType(sourceSettings.getCompilerKind())) {
            errorLater("Unsupported compiler type \"" + sourceSettings.getCompilerKind() + "\" for " + sourceSettings.getSourcePath());
            return;
        }

        taskRunner.runTask(new Task.Backgroundable(project, "Determining compiler version for " + sourceSettings.getSourceName()) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                String[] versionCommandLine = getVersionCommandLine(sourceSettings);
                try {
                    CompilerRunner versionRunner = new CompilerRunner(sourceSettings.getHost(), versionCommandLine, sourceSettings.getCompilerWorkingDir(), "", indicator, state.getCompilerTimeoutMillis());
                    String versionText = versionRunner.getStderr();
                    if (versionRunner.getExitCode() == 0 && !versionText.isEmpty()) {
                        String compilerVersion = parseCompilerVersion(sourceSettings.getCompilerKind(), versionText);
                        String compilerTarget = parseCompilerTarget(versionText);
                        if (!compilerVersion.isEmpty() && !compilerTarget.isEmpty()) {
                            LocalCompilerSettings newSettings = new LocalCompilerSettings(sourceSettings.getCompilerKind(), compilerVersion, compilerTarget);
                            ApplicationManager.getApplication().invokeLater(() -> {
                                state.getLocalCompilerSettings().put(new LocalCompilerPath(sourceSettings.getCompilerPath()), newSettings);
                                sourceCompilerSettingsConsumer.accept(new SourceCompilerSettings(sourceSettings, newSettings));
                            });
                        } else {
                            errorLater("Cannot parse compiler version:\n" + versionText);
                        }
                    } else {
                        errorLater("Cannot run compiler:\n" + String.join(" ", versionCommandLine) + "\nWorking directory:\n" + sourceSettings.getCompilerWorkingDir() + "\nExit code " + versionRunner.getExitCode() + "\nOutput:\n" + versionRunner.getStdout() + "Errors:\n" + versionText);
                    }
                } catch (ProcessCanceledException canceledException) {
                    //errorLater("Cannot determine compiler version:\n" + String.join(" ", versionCommandLine) + "\nCanceled");
                } catch (Exception exception) {
                    errorLater("Cannot determine compiler version:\n" + String.join(" ", versionCommandLine) + "\nException: " + exception.getMessage());
                }
            }
        });
    }

    @NotNull
    public Consumer<RefreshSignal> asRefreshSignalConsumer() {
        return refreshSignal -> {
            SettingsState state = CompilerExplorerSettingsProvider.getInstance(project).getState();
            state.setLocalCompilerSettings(SettingsState.EMPTY.getLocalCompilerSettings());
        };
    }

    @NotNull
    private static String[] getVersionCommandLine(@NotNull SourceSettings sourceSettings) {
        return Stream.of(
                sourceSettings.getCompilerPath(),
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

    private void errorLater(@NotNull String text) {
        ApplicationManager.getApplication().invokeLater(() -> errorConsumer.accept(new Error(text)));
    }
}
