package com.compilerexplorer.compiler;

import com.compilerexplorer.common.*;
import com.compilerexplorer.common.datamodel.SourceCompilerSettings;
import com.compilerexplorer.common.datamodel.SourceCompilerSettingsConsumer;
import com.compilerexplorer.common.datamodel.SourceSettings;
import com.compilerexplorer.common.datamodel.SourceSettingsConsumer;
import com.compilerexplorer.common.datamodel.state.LocalCompilerPath;
import com.compilerexplorer.common.datamodel.state.LocalCompilerSettings;
import com.compilerexplorer.common.datamodel.state.SettingsState;
import com.compilerexplorer.compiler.common.CompilerRunner;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.progress.Task;
import com.intellij.openapi.progress.impl.BackgroundableProcessIndicator;
import com.intellij.openapi.project.Project;
import com.jetbrains.cidr.lang.workspace.compiler.OCCompilerKind;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.stream.Stream;

import static com.jetbrains.cidr.lang.workspace.compiler.OCCompilerKind.CLANG;
import static com.jetbrains.cidr.lang.workspace.compiler.OCCompilerKind.GCC;

public class CompilerSettingsProducer implements SourceSettingsConsumer {
    @NotNull
    private final Project project;
    @NotNull
    private final SourceCompilerSettingsConsumer sourceCompilerSettingsConsumer;
    @Nullable
    private BackgroundableProcessIndicator currentProgressIndicator;

    public CompilerSettingsProducer(@NotNull Project project_, @NotNull SourceCompilerSettingsConsumer sourceCompilerSettingsConsumer_) {
        project = project_;
        sourceCompilerSettingsConsumer = sourceCompilerSettingsConsumer_;
    }

    @Override
    public void setSourceSetting(@NotNull SourceSettings sourceSettings) {
        SettingsState state = SettingsProvider.getInstance(project).getState();
        {
            LocalCompilerSettings existingSettings = state.getLocalCompilerSettings().get(new LocalCompilerPath(sourceSettings.getCompiler().getAbsolutePath()));
            if (existingSettings != null) {
                sourceCompilerSettingsConsumer.setSourceCompilerSetting(new SourceCompilerSettings(sourceSettings, existingSettings));
                return;
            }
        }

        if (!isSupportedCompilerType(sourceSettings.getCompilerKind())) {
            sourceCompilerSettingsConsumer.clearSourceCompilerSetting("Unsupported compiler type \"" + sourceSettings.getCompilerKind().toString() + "\" for " + sourceSettings.getSource().getPath());
            return;
        }

        sourceCompilerSettingsConsumer.clearSourceCompilerSetting("Determining compiler version...");
        File compiler = sourceSettings.getCompiler();
        File compilerWorkingDir = compiler.getParentFile();

        if (currentProgressIndicator != null) {
            currentProgressIndicator.cancel();
        }

        Task.Backgroundable task = new Task.Backgroundable(project, "Determining compiler version for " + sourceSettings.getSource().getPresentableName()) {
            @Override
            public void run(@NotNull ProgressIndicator indicator) {
                String[] versionCommandLine = getVersionCommandLine(sourceSettings);
                try {
                    CompilerRunner versionRunner = new CompilerRunner(versionCommandLine, compilerWorkingDir, "", indicator);
                    String versionText = versionRunner.getStderr();
                    if (versionRunner.getExitCode() == 0 && !versionText.isEmpty()) {
                        String compilerVersion = parseCompilerVersion(sourceSettings.getCompilerKind(), versionText);
                        String compilerTarget = parseCompilerTarget(versionText);
                        if (!compilerVersion.isEmpty() && !compilerTarget.isEmpty()) {
                            LocalCompilerSettings newSettings = new LocalCompilerSettings(sourceSettings.getCompilerKind().toString().toLowerCase(), compilerVersion, compilerTarget);
                            ApplicationManager.getApplication().invokeLater(() -> {
                                state.getLocalCompilerSettings().put(new LocalCompilerPath(sourceSettings.getCompiler().getAbsolutePath()), newSettings);
                                sourceCompilerSettingsConsumer.setSourceCompilerSetting(new SourceCompilerSettings(sourceSettings, newSettings));
                            });
                        } else {
                            ApplicationManager.getApplication().invokeLater(() -> sourceCompilerSettingsConsumer.clearSourceCompilerSetting("Cannot parse compiler version:\n" + versionText));
                        }
                    } else {
                        ApplicationManager.getApplication().invokeLater(() -> sourceCompilerSettingsConsumer.clearSourceCompilerSetting("Cannot run compiler:\n" + String.join(" ", versionCommandLine) + "\nWorking directory:\n" + compilerWorkingDir.getAbsolutePath() + "\nExit code " + versionRunner.getExitCode() + "\nOutput:\n" + versionRunner.getStdout() + "Errors:\n" + versionText));
                    }
                } catch (ProcessCanceledException canceledException) {
                    // empty
                } catch (Exception exception) {
                    ApplicationManager.getApplication().invokeLater(() -> sourceCompilerSettingsConsumer.clearSourceCompilerSetting("Cannot determine compiler version:\n" + String.join(" ", versionCommandLine) + "\nException: " + exception.getMessage()));
                }
            }
        };
        currentProgressIndicator = new BackgroundableProcessIndicator(task);
        ProgressManager.getInstance().runProcessWithProgressAsynchronously(task, currentProgressIndicator);
    }

    @Override
    public void clearSourceSetting(@NotNull String reason) {
        sourceCompilerSettingsConsumer.clearSourceCompilerSetting(reason);
    }

    @NotNull
    private static String[] getVersionCommandLine(@NotNull SourceSettings sourceSettings) {
        return Stream.of(
                sourceSettings.getCompiler().getAbsolutePath(),
                "-v"
        ).toArray(String[]::new);
    }

    private static boolean isSupportedCompilerType(@NotNull OCCompilerKind compilerKind) {
        return compilerKind == GCC || compilerKind == CLANG;
    }

    @NotNull
    private static String parseCompilerVersion(@NotNull OCCompilerKind compilerKind, @NotNull String versionText) {
        return versionText.replace('\n', ' ').replaceAll(".*" + compilerKind.toString().toLowerCase() + " version ([^ ]*).*", "$1");
    }

    @NotNull
    private static String parseCompilerTarget(@NotNull String versionText) {
        return versionText.replace('\n', ' ').replaceAll(".*Target: ([^-]*).*", "$1");
    }
}
