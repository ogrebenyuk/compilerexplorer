package com.compilerexplorer.common;

import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class TooltipUtil {
    @NonNls
    @NotNull
    public static final String NATIVE_LINE_BREAK = "\n";
    @NonNls
    @NotNull
    public static final String HTML_LINE_BREAK = "<br/>";
    @NonNls
    @NotNull
    public static final String NATIVE_INDENTATION = "  ";
    @NonNls
    @NotNull
    public static final String HTML_INDENTATION = "&nbsp;&nbsp;&nbsp;&nbsp;";

    @Nls
    @NotNull
    public static String prettify(@Nls @NotNull String uglyTooltipText) {
        return uglyTooltipText
                .replaceAll(NATIVE_LINE_BREAK, HTML_LINE_BREAK)
                .replaceAll(NATIVE_INDENTATION, HTML_INDENTATION);
    }
}
