package com.compilerexplorer.gui;

import com.compilerexplorer.common.*;
import com.compilerexplorer.datamodel.state.*;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.awt.*;

public class ToolWindowGui {
    public static final Key<ToolWindowGui> KEY = Key.create("compilerexplorer.ToolWindowGui");

    @NotNull
    private final Project project;
    @NotNull
    private final JPanel content;
    @NotNull
    private final TimerScheduler timerScheduler = new TimerScheduler();
    @NotNull
    private final SuppressionFlag suppressUpdates = new SuppressionFlag();
    @NotNull
    private final Runnable preprocessRequest;

    public ToolWindowGui(@NotNull Project project_,
                         @NotNull Component projectSourcesGuiComponent,
                         @NotNull Component matchesGuiComponent,
                         @NotNull Component editorGuiComponent,
                         @NotNull Runnable preprocessRequest_) {
        project = project_;
        preprocessRequest = preprocessRequest_;

        project.putUserData(ToolWindowGui.KEY, this);

        content = new JPanel(new BorderLayout());

        JPanel headPanel = new JPanel();
        headPanel.setLayout(new BoxLayout(headPanel, BoxLayout.X_AXIS));

        headPanel.add(projectSourcesGuiComponent);

        headPanel.add(matchesGuiComponent);

        AdditionalSwitchesGui additionalSwitchesGui = new AdditionalSwitchesGui(getState().getAdditionalSwitches(), this::additionalSwitchesUpdated);
        headPanel.add(additionalSwitchesGui.getComponent());

        ActionToolbar toolbar = ActionManager.getInstance().createActionToolbar(ActionPlaces.TOOLBAR, new DefaultActionGroup(ActionManager.getInstance().getAction("compilerexplorer.ToolbarGroup")), true);
        toolbar.setTargetComponent(headPanel);
        headPanel.add(toolbar.getComponent());

        content.add(headPanel, BorderLayout.NORTH);

        content.add(editorGuiComponent, BorderLayout.CENTER);

        maybeShowInitialNotice();
    }

    private void additionalSwitchesUpdated(@NotNull String text) {
        getState().setAdditionalSwitches(text);
        if (getState().getAutoupdateFromSource()) {
            schedulePreprocess();
        }
    }

    public void programTextChanged() {
        unlessUpdatesSuppressed(() -> {
            if (getState().getAutoupdateFromSource()) {
                schedulePreprocess();
            }
        });
    }

    private void schedulePreprocess() {
        timerScheduler.schedule(this::preprocess, getState().getDelayMillis());
    }

    private void preprocess() {
        ApplicationManager.getApplication().invokeLater(preprocessRequest);
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
}
