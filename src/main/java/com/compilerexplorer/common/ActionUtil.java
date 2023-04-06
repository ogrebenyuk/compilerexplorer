package com.compilerexplorer.common;

import com.intellij.ide.DataManager;
import com.intellij.openapi.actionSystem.*;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class ActionUtil {
    @NotNull
    public static AnAction findAction(@NonNls @NotNull String id) {
        return ActionManager.getInstance().getAction(id);
    }

    @NotNull
    public static AnActionEvent createEvent(@NotNull AnAction action, @NotNull Component component) {
        return AnActionEvent.createFromAnAction(action, null, ActionPlaces.UNKNOWN, DataManager.getInstance().getDataContext(component));
    }

    @NotNull
    public static Presentation createPresentation(@NotNull AnAction action, @NotNull Component component) {
        AnActionEvent event = createEvent(action, component);
        action.update(event);
        return event.getPresentation();
    }
}
