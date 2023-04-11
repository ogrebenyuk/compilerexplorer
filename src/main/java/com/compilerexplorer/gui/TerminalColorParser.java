package com.compilerexplorer.gui;

import com.intellij.openapi.editor.markup.EffectType;
import com.intellij.openapi.editor.markup.TextAttributes;
import com.intellij.ui.JBColor;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.VisibleForTesting;

import java.awt.*;
import java.util.Arrays;
import java.util.List;

public class TerminalColorParser {
    @NonNls
    @NotNull
    public static final String CSI_MARKER = "\u001B[";
    @NonNls
    public static final char END_OF_CODES = 'm';
    @NonNls
    @NotNull
    public static final String CODES_SEPARATOR = ";";
    @NonNls
    @NotNull
    public static final String PARAMETER_CHARS = "0123456789;";
    @NotNull
    public static final Color[] COLORS = new Color[] {
            JBColor.BLACK,
            JBColor.RED,
            JBColor.GREEN,
            JBColor.YELLOW,
            JBColor.BLUE,
            JBColor.MAGENTA,
            JBColor.CYAN,
            JBColor.WHITE
    };
    @NotNull
    public static final Color[] BRIGHT_COLORS = new Color[] {
            JBColor.BLACK.brighter(),
            JBColor.RED.brighter(),
            JBColor.GREEN.brighter(),
            JBColor.YELLOW.brighter(),
            JBColor.BLUE.brighter(),
            JBColor.MAGENTA.brighter(),
            JBColor.CYAN.brighter(),
            JBColor.WHITE.brighter()
    };

    public static class HighlightedRange {
        @NotNull
        public final TextAttributes textAttributes;
        public final int startOffset;
        public final int endOffset;

        public HighlightedRange(@NotNull TextAttributes textAttributes_, int startOffset_, int endOffset_) {
            textAttributes = textAttributes_;
            startOffset = startOffset_;
            endOffset = endOffset_;
        }
    }

    @NotNull
    public static String parse(@NotNull String text, @NotNull List<HighlightedRange> highlightedRanges) {
        final int len = text.length();
        StringBuilder builder = null;
        TextAttributes currentStyle = null;
        int rangeBegin = 0;
        int pos = 0;
        while (true) {
            int found = text.indexOf(CSI_MARKER, pos);
            if (found < 0) {
                break;
            } else {
                if (builder == null) {
                    builder = new StringBuilder(len);
                }
                builder.append(text, pos, found);
                found += CSI_MARKER.length();

                int end = found;
                while (end < len && PARAMETER_CHARS.indexOf(text.charAt(end)) != -1) {
                    ++end;
                }
                if (end + 1 < len && text.charAt(end) != END_OF_CODES) {
                    pos = end + 1;
                    continue;
                }

                if (end == len) {
                    pos = len;
                } else {
                    pos = end + 1;
                    if (currentStyle != null && builder.length() > rangeBegin) {
                        highlightedRanges.add(new HighlightedRange(currentStyle.clone(), rangeBegin, builder.length()));
                    }
                    rangeBegin = builder.length();
                    String subst = text.substring(found, end);
                    if (subst.isEmpty()) {
                        currentStyle = null;
                    }
                    List<Integer> codes = Arrays.stream(subst.split(CODES_SEPARATOR)).map(code -> code.isEmpty() ? 0 : Integer.parseInt(code)).toList();
                    for (int i = 0; i < codes.size(); ++i) {
                        int code = codes.get(i);
                        if (code == 0) {
                            currentStyle = null;
                        } else {
                            if (currentStyle == null) {
                                currentStyle = new TextAttributes();
                            }
                            switch (code) {
                                case 1 -> currentStyle.setFontType(Font.BOLD);
                                case 4 -> currentStyle.setEffectType(EffectType.LINE_UNDERSCORE);
                                case 22 -> currentStyle.setFontType(Font.PLAIN);
                                case 24 -> currentStyle.setEffectType(null);
                                case 30, 31, 32, 33, 34, 35, 36, 37 -> currentStyle.setForegroundColor(COLORS[code - 30]);
                                case 38 -> i = setColor(currentStyle, codes, i, true);
                                case 39 -> currentStyle.setForegroundColor(null);
                                case 40, 41, 42, 43, 44, 45, 46, 47 -> currentStyle.setBackgroundColor(COLORS[code - 40]);
                                case 48 -> i = setColor(currentStyle, codes, i, false);
                                case 49 -> currentStyle.setBackgroundColor(null);
                                case 90, 91, 92, 93, 94, 95, 96, 97 -> currentStyle.setForegroundColor(BRIGHT_COLORS[code - 90]);
                                case 100, 101, 102, 103, 104, 105, 106, 107 -> currentStyle.setBackgroundColor(BRIGHT_COLORS[code - 100]);
                            }
                        }
                    }
                }
            }
        }
        if (builder != null) {
            if (pos < len) {
                builder.append(text, pos, len);
            }
            if (currentStyle != null && builder.length() > rangeBegin) {
                highlightedRanges.add(new HighlightedRange(currentStyle.clone(), rangeBegin, builder.length()));
            }
            return builder.toString();
        } else {
            return text;
        }
    }

    private static int setColor(@NotNull TextAttributes style, @NotNull List<Integer> codes, int i, boolean foreground) {
        Color color = null;
        if (i + 1 < codes.size()) {
            i += 1;
            int colorType = codes.get(i);
            if (colorType == 2) {
                if (i + 3 < codes.size()) {
                    color = color(codes.get(i + 1), codes.get(i + 2), codes.get(i + 3));
                    i += 3;
                } else {
                    i = codes.size();
                }
            } else if (colorType == 5) {
                if (i + 1 < codes.size()) {
                    i += 1;
                    int color256 = codes.get(i);
                    if (color256 < 8) {
                        color = COLORS[color256];
                    } else if (color256 < 16) {
                        color = BRIGHT_COLORS[color256 - 8];
                    } else if (color256 < 232) {
                        color256 -= 16;
                        color = color(((color256 / 36) % 6) * 51, ((color256 / 6) % 6) * 51, (color256 % 6) * 51);
                    } else if (color256 < 256) {
                        color256 -= 232;
                        color256 *= 11;
                        color = color(color256, color256, color256);
                    }
                }
            }
        }
        if (color != null) {
            if (foreground) {
                style.setForegroundColor(color);
            } else {
                style.setBackgroundColor(color);
            }
        }
        return i;
    }

    @VisibleForTesting
    @NotNull
    public static Color color(int r, int g, int b) {
        return new JBColor(new Color(r, g, b), new Color(r, g, b));
    }
}
