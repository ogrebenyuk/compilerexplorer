package com.compilerexplorer.gui;

import com.compilerexplorer.common.*;
import com.compilerexplorer.datamodel.state.*;
import com.compilerexplorer.gui.listeners.*;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.colors.*;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.markup.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.util.Producer;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class ToolWindowGui {
    public static final Key<ToolWindowGui> KEY = Key.create(Constants.PROJECT_TITLE + ".ToolWindowGui");

    @NotNull
    private final Project project;
    @NotNull
    private final JPanel content;
    @NotNull
    private final Consumer<RefreshSignal> refreshSignalConsumer;
    @NotNull
    private final TimerScheduler timerScheduler = new TimerScheduler();
    @NotNull
    private final SuppressionFlag suppressUpdates = new SuppressionFlag();

    public ToolWindowGui(@NotNull Project project_, @NotNull Consumer<RefreshSignal> refreshSignalConsumer_,
                         @NotNull Component projectSettingsGuiComponent,
                         @NotNull Component matchesGuiComponent,
                         @NotNull Component editorGuiComponent,
                         @NotNull Producer<EditorEx> editorProducer) {
        project = project_;
        refreshSignalConsumer = refreshSignalConsumer_;

        project.putUserData(ToolWindowGui.KEY, this);

        content = new JPanel(new BorderLayout());

        maybeMigrateColorSettings();

        JPanel headPanel = new JPanel();
        headPanel.setLayout(new BoxLayout(headPanel, BoxLayout.X_AXIS));

        headPanel.add(projectSettingsGuiComponent);

        headPanel.add(matchesGuiComponent);

        AdditionalSwitchesGui additionalSwitchesGui = new AdditionalSwitchesGui(getState().getAdditionalSwitches(), this::additionalSwitchesUpdated);
        headPanel.add(additionalSwitchesGui.getComponent());

        ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar(ActionPlaces.TOOLBAR, new DefaultActionGroup(ActionManager.getInstance().getAction("compilerexplorer.ToolbarGroup")), true);
        toolbar.setTargetComponent(headPanel);
        headPanel.add(toolbar.getComponent());

        content.add(headPanel, BorderLayout.NORTH);

        content.add(editorGuiComponent, BorderLayout.CENTER);

        new EditorChangeListener(project, editorProducer, this::programTextChanged);

        maybeShowInitialNotice();
    }

    public void resetCacheAndReload() {
        refreshSignalConsumer.accept(RefreshSignal.RESET);
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
        ApplicationManager.getApplication().invokeLater(() -> refreshSignalConsumer.accept(RefreshSignal.COMPILE));
    }

    public void preprocess() {
        ApplicationManager.getApplication().invokeLater(() -> refreshSignalConsumer.accept(RefreshSignal.PREPROCESS));
    }

    @NotNull
    public JComponent getContent() {
        return content;
    }

    @NotNull
    private SettingsState getState() {
        return CompilerExplorerSettingsProvider.getInstance(project).getState();
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
