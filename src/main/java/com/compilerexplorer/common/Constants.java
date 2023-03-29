package com.compilerexplorer.common;

import com.google.common.collect.ImmutableMap;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.ui.JBColor;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.ResourceBundle;

public class Constants {
    @NotNull
    public static final ResourceBundle BUNDLE = ResourceBundle.getBundle("messages.compilerexplorerBundle");
    @NotNull
    public static final String PROJECT_TITLE = BUNDLE.getString("toolwindow.stripe.compilerexplorer");
    @NotNull
    public static final String DEFAULT_URL = "http://localhost:10240";
    @NotNull
    public static final String DEFAULT_URL_DESCRIPTION = BUNDLE.getString("compilerexplorer.LocalUrlDescription");
    @NotNull
    public static final String PUBLIC_URL = "https://godbolt.org";
    @NotNull
    public static final String PUBLIC_URL_DESCRIPTION = BUNDLE.getString("compilerexplorer.PublicUrlDescription");
    @NotNull
    public static final String PUBLIC_URL2 = "https://compiler-explorer.org";
    @NotNull
    public static final String PUBLIC_URL2_DESCRIPTION = BUNDLE.getString("compilerexplorer.PublicUrlDescription2");
    @NotNull
    public static final Map<String, String> DEFAULT_URLS = ImmutableMap.of(
            DEFAULT_URL, DEFAULT_URL_DESCRIPTION,
            PUBLIC_URL, PUBLIC_URL_DESCRIPTION,
            PUBLIC_URL2, PUBLIC_URL2_DESCRIPTION);
    @NotNull
    public static final String DEFAULT_ADDITIONAL_SWITCHES = "-fverbose-asm";
    @NotNull
    public static final String DEFAULT_IGNORE_SWITCHES = "";
    @NotNull
    public static final String HIGHLIGHT_KEY_NAME = "Editor.compilerExplorerHighlightBackground";
    @NotNull
    public static final JBColor DEFAULT_HIGHLIGHT_COLOR = new JBColor(JBColor.CYAN.brighter(), JBColor.CYAN.darker());
    @NotNull
    public static final TextAttributesKey HIGHLIGHT_COLOR = createHighlightColor();
    @NotNull
    public static final String COLOR_SETTINGS_TITLE = PROJECT_TITLE + " Colors";
    public static final long DEFAULT_DELAY_MILLIS = 1000;
    public static final int DEFAULT_COMPILER_TIMEOUT_MILLIS = 60000;
    @NotNull
    public static final String NOTIFICATION_GROUP_NAME = "compilerexplorer";
    @NotNull
    public static final String INITIAL_NOTICE = "Default " + PROJECT_TITLE + " URL is set to \"" + DEFAULT_URL + "\" for privacy and can be changed on the settings page.";

    @NotNull
    public static final NotificationGroup NOTIFICATION_GROUP = NotificationGroupManager.getInstance().getNotificationGroup(NOTIFICATION_GROUP_NAME);

    @NotNull
    private static TextAttributesKey createHighlightColor() {
        TextAttributesKey key = TextAttributesKey.createTextAttributesKey(HIGHLIGHT_KEY_NAME);
        key.getDefaultAttributes().setBackgroundColor(DEFAULT_HIGHLIGHT_COLOR);
        return key;
    }
}
