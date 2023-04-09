package com.compilerexplorer.common;

import com.google.common.collect.ImmutableMap;
import com.intellij.icons.AllIcons;
import com.intellij.notification.NotificationGroup;
import com.intellij.notification.NotificationGroupManager;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.ui.IconManager;
import com.intellij.ui.JBColor;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.Map;

public class Constants {
    @NonNls
    @NotNull
    public static final String DEFAULT_URL = "http://localhost:10240";
    @NotNull
    public static final Map<String, String> DEFAULT_URLS = ImmutableMap.of(
            DEFAULT_URL, Bundle.get("compilerexplorer.Constants.LocalUrlDescription"),
            "https://godbolt.org", Bundle.get("compilerexplorer.Constants.PublicUrlDescription"),
            "https://compiler-explorer.org", Bundle.get("compilerexplorer.Constants.PublicUrlDescription2"));
    @NonNls
    @NotNull
    public static final String DEFAULT_ADDITIONAL_SWITCHES = "-fverbose-asm";
    @NonNls
    @NotNull
    public static final String DEFAULT_IGNORE_SWITCHES = "";
    @NonNls
    @NotNull
    public static final String HIGHLIGHT_KEY_NAME = "Editor.compilerExplorerHighlightBackground";
    @NotNull
    public static final JBColor DEFAULT_HIGHLIGHT_COLOR = new JBColor(JBColor.CYAN.brighter(), JBColor.CYAN.darker());
    @NotNull
    public static final TextAttributesKey HIGHLIGHT_COLOR = createHighlightColor();
    @Nls
    @NotNull
    public static final String COLOR_SETTINGS_TITLE = Bundle.get("compilerexplorer.Constants.ColorsTitle");
    public static final long DEFAULT_DELAY_MILLIS = 1000;
    public static final int DEFAULT_COMPILER_TIMEOUT_MILLIS = 60000;
    @NonNls
    @NotNull
    public static final String NOTIFICATION_GROUP_NAME = "compilerexplorer";
    @Nls
    @NotNull
    public static final String INITIAL_NOTICE = Bundle.format("compilerexplorer.Constants.InitialNotice", "DefaultUrl", DEFAULT_URL);
    @NonNls
    @NotNull
    public static final String DEFAULT_PREPROCESSED_TEXT_EXTENSION = "ii";
    @NotNull
    public static final Icon TAB_ERROR_ICON = AllIcons.General.Error;
    @NotNull
    public static final Icon EMPTY_ICON = IconManager.getInstance().createEmptyIcon(TAB_ERROR_ICON);

    @NotNull
    public static final NotificationGroup NOTIFICATION_GROUP = NotificationGroupManager.getInstance().getNotificationGroup(NOTIFICATION_GROUP_NAME);

    @NotNull
    private static TextAttributesKey createHighlightColor() {
        TextAttributesKey key = TextAttributesKey.createTextAttributesKey(HIGHLIGHT_KEY_NAME);
        key.getDefaultAttributes().setBackgroundColor(DEFAULT_HIGHLIGHT_COLOR);
        return key;
    }
}
