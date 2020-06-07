package com.compilerexplorer.common;

import com.intellij.notification.NotificationGroup;
import com.intellij.ui.JBColor;
import org.jetbrains.annotations.NotNull;

import java.awt.*;

public class Constants {
    @NotNull
    public static final String PROJECT_TITLE = "Compiler Explorer";
    @NotNull
    public static final String DEFAULT_URL = "http://localhost:10240";
    @NotNull
    public static final String DEFAULT_ADDITIONAL_SWITCHES = "-fverbose-asm";
    @NotNull
    public static final String DEFAULT_IGNORE_SWITCHES = "";
    @NotNull
    public static final Color DEFAULT_HIGHLIGHT_COLOR = JBColor.CYAN;
    public static final long DEFAULT_DELAY_MILLIS = 1000;
    @NotNull
    public static final String NOTIFICATION_GROUP_NAME = PROJECT_TITLE;
    @NotNull
    public static final String INITIAL_NOTICE = "Default " + PROJECT_TITLE + " URL is set to \"" + DEFAULT_URL + "\" for privacy and can be changed on the settings page.";

    @NotNull
    public static final NotificationGroup NOTIFICATION_GROUP = NotificationGroup.balloonGroup(NOTIFICATION_GROUP_NAME);
}
