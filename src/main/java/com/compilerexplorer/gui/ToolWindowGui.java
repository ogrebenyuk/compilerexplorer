package com.compilerexplorer.gui;

import com.compilerexplorer.common.*;
import com.compilerexplorer.datamodel.*;
import com.compilerexplorer.datamodel.state.*;
import com.compilerexplorer.gui.listeners.*;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.colors.*;
import com.intellij.openapi.editor.markup.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.lang.Error;
import java.util.function.Consumer;

public class ToolWindowGui {
    public static final Key<ToolWindowGui> KEY = Key.create(Constants.PROJECT_TITLE + " ToolWindowGui");

    @NotNull
    private final Project project;
    @NotNull
    private final JPanel content;
    @NotNull
    private final ProjectSettingsGui projectSettingsGui;
    @NotNull
    private final MatchesGui matchesGui;
    @NotNull
    private final EditorGui editorGui;
    //@Nullable
    //private Consumer<PreprocessedSource> preprocessedSourceConsumer;
    @Nullable
    private Consumer<RefreshSignal> refreshSignalConsumer;
    @NotNull
    private final TimerScheduler timerScheduler = new TimerScheduler();
    @NotNull
    private final SuppressionFlag suppressUpdates = new SuppressionFlag();

    public ToolWindowGui(@NotNull Project project_) {
        project = project_;
        content = new JPanel(new BorderLayout());

        project.putUserData(KEY, this);

        maybeMigrateColorSettings();

        JPanel headPanel = new JPanel();
        headPanel.setLayout(new BoxLayout(headPanel, BoxLayout.X_AXIS));

        projectSettingsGui = new ProjectSettingsGui(suppressUpdates);
        headPanel.add(projectSettingsGui.getComponent());

        matchesGui = new MatchesGui(suppressUpdates);
        headPanel.add(matchesGui.getComponent());

        AdditionalSwitchesGui additionalSwitchesGui = new AdditionalSwitchesGui(getState().getAdditionalSwitches(), this::additionalSwitchesUpdated);
        headPanel.add(additionalSwitchesGui.getComponent());

        ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar(ActionPlaces.TOOLBAR, new DefaultActionGroup(ActionManager.getInstance().getAction("compilerexplorer.ToolbarGroup")), true);
        toolbar.setTargetComponent(headPanel);
        headPanel.add(toolbar.getComponent());

        content.add(headPanel, BorderLayout.NORTH);

        editorGui = new EditorGui(project, suppressUpdates);
        project.putUserData(EditorGui.KEY, editorGui);
        content.add(editorGui.getComponent(), BorderLayout.CENTER);

        new EditorChangeListener(project, this::programTextChanged);

        maybeShowInitialNotice();
    }

    public void resetCacheAndReload() {
        if (refreshSignalConsumer != null) {
            refreshSignalConsumer.accept(RefreshSignal.RESET);
        }
    }

    private void additionalSwitchesUpdated(@NotNull String text) {
        getState().setAdditionalSwitches(text);
        if (getState().getAutoupdateFromSource()) {
            schedulePreprocess();
        }
    }

    private void programTextChanged() {
        unlessUpdatesSuppressed(() ->
            ApplicationManager.getApplication().invokeLater(() -> {
                SettingsState state = getState();
                if (state.getEnabled() && state.getAutoupdateFromSource()) {
                    schedulePreprocess();
                }
        }));
    }

    private void schedulePreprocess() {
        timerScheduler.schedule(this::preprocess, getState().getDelayMillis());
    }

    public void recompile() {
        if (refreshSignalConsumer != null) {
            ApplicationManager.getApplication().invokeLater(() -> refreshSignalConsumer.accept(RefreshSignal.COMPILE));
        }
    }

    public void preprocess() {
        if (refreshSignalConsumer != null) {
            ApplicationManager.getApplication().invokeLater(() -> refreshSignalConsumer.accept(RefreshSignal.PREPROCESS));
        }
    }
/*
    private void selectCompilerMatchAndRecompile(@NotNull CompilerMatch compilerMatch) {
        matchesComboBox.setToolTipText(getMatchTooltip(compilerMatch));
        if (preprocessedSourceConsumer != null && compiledText != null) {
            preprocessedSourceConsumer.accept(
                    new PreprocessedSource(
                            new SourceRemoteMatched(
                                    compiledText.getPreprocessedSource().getSourceRemoteMatched().getSourceCompilerSettings(),
                                    new CompilerMatches(
                                            compilerMatch,
                                            compiledText.getPreprocessedSource().getSourceRemoteMatched().getRemoteCompilerMatches().getOtherMatches()
                                    )
                            ),
                            compiledText.getPreprocessedSource().getPreprocessedText()
                    )
            );
        } else {
            selectCompilerMatch(compilerMatch);
        }
    }
*/
    public void setSourceSettingsConsumer(@NotNull Consumer<SourceSettings> sourceSettingsConsumer) {
        projectSettingsGui.setSourceSettingsConsumer(s -> {
            if (s != null) {
                sourceSettingsConsumer.accept(s);
            } else {
                editorGui.showError("No source selected");
            }
        });
    }

    public void setSourceRemoteMatchedConsumer(@NotNull Consumer<SourceRemoteMatched> sourceRemoteMatchedConsumer) {
        matchesGui.setSourceRemoteMatchedConsumer(s -> {
            if (s != null) {
                sourceRemoteMatchedConsumer.accept(s);
            } else {
                editorGui.showError("No compiler selected");
            }
        });
    }

    public void setPreprocessedSourceConsumer(@NotNull Consumer<PreprocessedSource> preprocessedSourceConsumer_) {
        //preprocessedSourceConsumer = preprocessedSourceConsumer_;
    }

    public void setRefreshSignalConsumer(@NotNull Consumer<RefreshSignal> refreshSignalConsumer_) {
        refreshSignalConsumer = refreshSignalConsumer_;
    }

    @NotNull
    public JComponent getContent() {
        return content;
    }

    @NotNull
    public Consumer<RefreshSignal> asResetSignalConsumer() {
        return projectSettingsGui.asResetSignalConsumer();
    }

    @NotNull
    public Consumer<RefreshSignal> asReconnectSignalConsumer() {
        return matchesGui.asReconnectSignalConsumer();
    }

    @NotNull
    public Consumer<RefreshSignal> asRecompileSignalConsumer() {
        return refreshSignal -> {
            ApplicationManager.getApplication().assertIsDispatchThread();
            matchesGui.clearSourceRemoteMatched();
            editorGui.clearCompiledText();
        };
    }

    @NotNull
    public Consumer<ProjectSettings> asProjectSettingsConsumer() {
        return projectSettingsGui;
    }

    @NotNull
    public Consumer<SourceRemoteMatched> asSourceRemoteMatchedConsumer() {
        return matchesGui;
    }

    @NotNull
    public Consumer<CompiledText> asCompiledTextConsumer() {
        return editorGui;
    }

    @NotNull
    public Consumer<Error> asErrorConsumer() {
        return error -> editorGui.showError(error.getMessage());
    }

    @NotNull
    private SettingsState getState() {
        return CompilerExplorerSettingsProvider.getInstance(project).getState();
    }

    @NotNull
    private Filters getFilters() {
        return getState().getFilters();
    }

    private void unlessUpdatesSuppressed(Runnable runnable) {
        suppressUpdates.unlessApplied(runnable);
    }

    private void maybeShowInitialNotice() {
        if (!getState().getInitialNoticeShown()) {
            showInitialNotice();
            getState().setInitialNoticeShown(true);
        }
    }

    private void showInitialNotice() {
        Constants.NOTIFICATION_GROUP
                .createNotification(Constants.INITIAL_NOTICE, NotificationType.INFORMATION)
                .addAction(ActionManager.getInstance().getAction("compilerexplorer.ShowUrlHistoryInSettings"))
                .notify(project);
    }

    private void maybeMigrateColorSettings() {
        if (getState().getHighlightColorRGB() != SettingsState.NO_SAVED_COLOR) {
            migrateColorSettings();
            getState().setHighlightColorRGB(SettingsState.NO_SAVED_COLOR);
        }
    }

    private void migrateColorSettings() {
        TextAttributes migrated = new TextAttributes();
        migrated.copyFrom(EditorColorsManager.getInstance().getGlobalScheme().getAttributes(Constants.HIGHLIGHT_COLOR));
        migrated.setBackgroundColor(new Color(getState().getHighlightColorRGB()));
        EditorColorsManager.getInstance().getGlobalScheme().setAttributes(Constants.HIGHLIGHT_COLOR, migrated);
    }
}
