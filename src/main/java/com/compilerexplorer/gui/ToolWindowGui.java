package com.compilerexplorer.gui;

import com.compilerexplorer.common.*;
import com.compilerexplorer.datamodel.*;
import com.compilerexplorer.datamodel.state.*;
import com.compilerexplorer.gui.listeners.*;
import com.intellij.icons.AllIcons;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.colors.*;
import com.intellij.openapi.editor.markup.*;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.lang.Error;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class ToolWindowGui {
    @NotNull
    private final Project project;
    @NotNull
    private final JPanel content;
    @NotNull
    private final ProjectSettingsGui projectSettingsGui;
    @NotNull
    private final MatchesGui matchesGui;
    @NotNull
    private final SquareButtonGui settingsGui;
    @NotNull
    private final EditorGui editorGui;
    @Nullable
    private Consumer<PreprocessedSource> preprocessedSourceConsumer;
    @Nullable
    private Consumer<RefreshSignal> refreshSignalConsumer;
    @NotNull
    private final TimerScheduler timerScheduler = new TimerScheduler();
    @NotNull
    private final SuppressionFlag suppressUpdates = new SuppressionFlag();
    @NotNull
    private final DefaultActionGroup settingsActions = new DefaultActionGroup();
    @NotNull
    private final DefaultActionGroup gutterActions = new DefaultActionGroup();
    @NotNull
    private static final AnAction showSettingsAction = new AnAction(Constants.PROJECT_TITLE + " Settings...") {
        @Override
        public void actionPerformed(@NotNull AnActionEvent event) {
            ShowSettingsUtil.getInstance().showSettingsDialog(event.getProject(), Constants.PROJECT_TITLE);
        }
        @Override
        public void update(@NotNull AnActionEvent event) {
            event.getPresentation().setIcon(AllIcons.General.Settings);
        }
        @Override
        public @NotNull ActionUpdateThread getActionUpdateThread() {
            return ActionUpdateThread.BGT;
        }
    };
    @NotNull
    private final AnAction scrollFromSourceAction = new AnAction("Scroll from Source") {
        @Override
        public void actionPerformed(@NotNull AnActionEvent event) {
            editorGui.scrollFromSource();
        }
        @Override
        public void update(@NotNull AnActionEvent event) {
            event.getPresentation().setIcon(AllIcons.General.Locate);
            event.getPresentation().setVisible(!getState().getAutoscrollFromSource());
        }
        @Override
        public @NotNull ActionUpdateThread getActionUpdateThread() {
            return ActionUpdateThread.BGT;
        }
    };

    public ToolWindowGui(@NotNull Project project_) {
        project = project_;
        content = new JPanel(new BorderLayout());

        maybeMigrateColorSettings();

        JPanel headPanel = new JPanel();
        headPanel.setLayout(new BoxLayout(headPanel, BoxLayout.X_AXIS));

        projectSettingsGui = new ProjectSettingsGui(suppressUpdates);
        headPanel.add(projectSettingsGui.getComponent());

        matchesGui = new MatchesGui(suppressUpdates);
        headPanel.add(matchesGui.getComponent());

        AdditionalSwitchesGui additionalSwitchesGui = new AdditionalSwitchesGui(getState().getAdditionalSwitches(), this::additionalSwitchesUpdated);
        headPanel.add(additionalSwitchesGui.getComponent());

        SquareButtonGui recompileGui = new SquareButtonGui(AllIcons.Actions.Refresh, "Recompile current source", this::preprocess);
        headPanel.add(recompileGui.getComponent());

        settingsActions.add(Separator.create("Output"));
        addToggleAction("Compile to binary object and disassemble the output", this::getFilters, Filters::getBinaryObject, Filters::setBinaryObject, true, false, false, false);
        addToggleAction("Link to binary and disassemble the output", this::getFilters, Filters::getBinary, Filters::setBinary, true, false, false, false);
        addToggleAction("Execute code and show its output", this::getFilters, Filters::getExecute, Filters::setExecute, true, false, false, false);
        addToggleAction("Output disassembly in Intel syntax", this::getFilters, Filters::getIntel, Filters::setIntel, true, false, false, false);
        addToggleAction("Demangle output", this::getFilters, Filters::getDemangle, Filters::setDemangle, true, false, false, false);
        settingsActions.add(Separator.create());
        settingsActions.add(Separator.create("Filter"));
        addToggleAction("Filter unused labels from the output", this::getFilters, Filters::getLabels, Filters::setLabels, true, false, false, false);
        addToggleAction("Filter functions from other libraries from the output", this::getFilters, Filters::getLibraryCode, Filters::setLibraryCode, true, false, false, false);
        addToggleAction("Filter all assembler directives from the output", this::getFilters, Filters::getDirectives, Filters::setDirectives, true, false, false, false);
        addToggleAction("Remove all lines which are only comments from the output", this::getFilters, Filters::getCommentOnly, Filters::setCommentOnly, true, false, false, false);
        addToggleAction("Trim intra-line whitespace", this::getFilters, Filters::getTrim, Filters::setTrim, true, false, false, false);
        settingsActions.add(Separator.create());
        settingsActions.add(Separator.create("Appearance"));
        addToggleAction("Show line numbers", this::getState, SettingsState::getShowLineNumbers, SettingsState::setShowLineNumbers, false, false, true, false);
        addToggleAction("Show byte offsets in disassembled output", this::getState, SettingsState::getShowByteOffsets, SettingsState::setShowByteOffsets, false, false, true, false);
        addToggleAction("Show source location", this::getState, SettingsState::getShowSourceAnnotations, SettingsState::setShowSourceAnnotations, false, false, true, false);
        addToggleAction("Shorten Templates", this::getState, SettingsState::getShortenTemplates, SettingsState::setShortenTemplates, false, true, false, false);
        addToggleAction("Enable folding", this::getState, SettingsState::getEnableFolding, SettingsState::setEnableFolding, false, false, false, true);
        AnAction expandAllAction = createExpandAllFoldingAction("Expand all folding", true);
        settingsActions.add(expandAllAction);
        gutterActions.add(expandAllAction);
        AnAction collapseAllAction = createExpandAllFoldingAction("Collapse all folding", false);
        settingsActions.add(collapseAllAction);
        gutterActions.add(collapseAllAction);
        settingsActions.add(Separator.create());
        settingsActions.add(Separator.create("Behavior"));
        addToggleAction("Autoscroll to Source", this::getState, SettingsState::getAutoscrollToSource, SettingsState::setAutoscrollToSource, false, false, false, false);
        addToggleAction("Autoscroll from Source", this::getState, SettingsState::getAutoscrollFromSource, SettingsState::setAutoscrollFromSource, false, false, false, false);
        addToggleAction("Autoupdate from Source", this::getState, SettingsState::getAutoupdateFromSource, SettingsState::setAutoupdateFromSource, false, false, false, false);
        settingsActions.add(showSettingsAction);

        settingsGui = new SquareButtonGui(AllIcons.Actions.More, "Options", this::showSettingsPopupMenu);
        headPanel.add(settingsGui.getComponent());

        content.add(headPanel, BorderLayout.NORTH);

        editorGui = new EditorGui(project, suppressUpdates, gutterActions);
        content.add(editorGui.getComponent(), BorderLayout.CENTER);

        new EditorChangeListener(project, this::programTextChanged);

        settingsActions.add(new AnAction("Reset Cache and Reload") {
            @Override
            public void actionPerformed(@NotNull AnActionEvent event) {
                if (refreshSignalConsumer != null) {
                    refreshSignalConsumer.accept(RefreshSignal.RESET);
                }
            }
            @Override
            public void update(@NotNull AnActionEvent event) {
                event.getPresentation().setIcon(AllIcons.Actions.ForceRefresh);
            }
            @Override
            public @NotNull ActionUpdateThread getActionUpdateThread() {
                return ActionUpdateThread.BGT;
            }
        });

        maybeShowInitialNotice();
    }

    @NotNull
    public DefaultActionGroup getSettingsActions() {
        return settingsActions;
    }

    @NotNull
    public AnAction getScrollFromSourceAction() {
        return scrollFromSourceAction;
    }

    private void showSettingsPopupMenu() {
        EditorGui.showPopupMenu(ActionPlaces.TOOLWINDOW_POPUP, settingsActions, settingsGui.getComponent(), 0, settingsGui.getComponent().getHeight());
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

    private <T> void addToggleAction(
            @NotNull String text,
            @NotNull Supplier<T> supplier,
            @NotNull Function<T, Boolean> getter,
            @NotNull BiConsumer<T, Boolean> setter,
            boolean recompile,
            boolean reparse,
            boolean updateGutter,
            boolean updateFolding
    ) {
        AnAction action = new ToggleAction(text) {
            @Override
            public boolean isSelected(@NotNull AnActionEvent event) {
                return getter.apply(supplier.get());
            }
            @Override
            public void setSelected(@NotNull AnActionEvent event, boolean selected) {
                setter.accept(supplier.get(), selected);
                if (recompile && refreshSignalConsumer != null) {
                    refreshSignalConsumer.accept(RefreshSignal.COMPILE);
                }
                if (reparse) {
                    editorGui.reparse();
                }
                if (updateGutter) {
                    editorGui.updateGutter();
                }
                if (updateFolding) {
                    editorGui.updateFolding();
                }
            }
            @Override
            public @NotNull ActionUpdateThread getActionUpdateThread() {
                return ActionUpdateThread.BGT;
            }
        };
        settingsActions.add(action);
        if (updateGutter || updateFolding) {
            gutterActions.add(action);
        }
    }

    private void schedulePreprocess() {
        timerScheduler.schedule(this::preprocess, getState().getDelayMillis());
    }

    private void preprocess() {
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
        preprocessedSourceConsumer = preprocessedSourceConsumer_;
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

    @NotNull
    AnAction createExpandAllFoldingAction(@NotNull String text, boolean isExpanded) {
        return new AnAction(text) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                editorGui.expandAllFolding(isExpanded);
            }
            @Override
            public void update(@NotNull AnActionEvent event) {
                event.getPresentation().setEnabled(getState().getEnableFolding());
            }
            @Override
            public @NotNull ActionUpdateThread getActionUpdateThread() {
                return ActionUpdateThread.BGT;
            }
        };
    }

    private void maybeShowInitialNotice() {
        if (!getState().getInitialNoticeShown()) {
            showInitialNotice();
            getState().setInitialNoticeShown(true);
        }
    }

    private void showInitialNotice() {
        Notifications.Bus.notify(Constants.NOTIFICATION_GROUP.createNotification(Constants.INITIAL_NOTICE, NotificationType.INFORMATION).addAction(showSettingsAction), project);
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
