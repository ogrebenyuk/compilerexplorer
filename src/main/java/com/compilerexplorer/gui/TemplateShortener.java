package com.compilerexplorer.gui;

import org.jetbrains.annotations.NotNull;

class TemplateShortener {
    static void shortenTemplates(@NotNull StringBuilder builder, @NotNull String text) {
        int length = text.length();
        int depth = 0;
        boolean isAddress = false;
        for (int i = 0; i < length; ++i) {
            char c = text.charAt(i);
            boolean isMinDepth = isMinDepth(depth, isAddress);
            if (c == '<') {
                if (isOperator(text, i)) {
                    if (isMinDepth) {
                        builder.append(c);
                    }
                    if (i + 1 < length && text.charAt(i + 1) == c) {
                        if (isMinDepth) {
                            builder.append(c);
                        }
                        ++i;
                    }
                } else {
                    if (depth == 0) {
                        isAddress = isAddress(text, i);
                    }
                    if (isMinDepth) {
                        builder.append(c);
                        if (!isAddress || depth > 0) {
                            builder.append("...");
                        }
                    }
                    depth++;
                }
            } else if (c == '>') {
                if (isOperator(text, i)) {
                    if (isMinDepth) {
                        builder.append(c);
                    }
                    if (i + 1 < length && text.charAt(i + 1) == c) {
                        if (isMinDepth) {
                            builder.append(c);
                        }
                        ++i;
                    }
                } else {
                    depth--;
                    if (depth == 0) {
                        isAddress = false;
                    }
                    isMinDepth = isMinDepth(depth, isAddress);
                    if (isMinDepth) {
                        builder.append(c);
                    }
                }
            } else {
                if (isMinDepth) {
                    builder.append(c);
                }
            }
        }
    }

    private static boolean isOperator(@NotNull String text, int i) {
        return ((i >= 8 && text.charAt(i - 1) == 'r' && text.startsWith("operator", i - 8)) ||
                (i >= 1 && text.charAt(i - 1) == '-') ||
                (i >= 10 && text.charAt(i - 1) == '=' && text.startsWith("operator<=", i - 10))
        );
    }

    private static boolean isMinDepth(int depth, boolean isAddress) {
        return depth == (isAddress ? 1 : 0);
    }

    private static boolean isAddress(@NotNull String text, int openingBracket) {
        if (openingBracket >= 2) {
            int closingBracket = findClosingBracket(text, openingBracket);
            if (closingBracket >= openingBracket + 5) {
                int intPos1 = openingBracket - 2;
                int intPos2 = closingBracket - 1;
                if (text.charAt(intPos2) == 'x') {
                    return false;
                }
                while (text.charAt(intPos2) != 'x') {
                    if ((intPos1 < 0) || (intPos2 <= openingBracket + 1) || (text.charAt(intPos1) != text.charAt(intPos2))) {
                        return false;
                    }
                    --intPos1;
                    --intPos2;
                }
                --intPos2;
                if ((intPos2 <= openingBracket + 1) || (text.charAt(intPos2) != '0')) {
                    return false;
                }
                --intPos2;
                return (intPos2 > openingBracket + 1) && ((text.charAt(intPos2) == '+') || (text.charAt(intPos2) == '-'));
            }
        }
        return false;
    }

    static int findClosingBracket(@NotNull String text, int openingBracket) {
        int length = text.length();
        int depth = 0;
        for (int i = openingBracket; i < length; ++i) {
            char c = text.charAt(i);
            if (c == '<') {
                if (isOperator(text, i)) {
                    if (i + 1 < length && text.charAt(i + 1) == c) {
                        ++i;
                    }
                } else {
                    depth++;
                }
            } else if (c == '>') {
                if (isOperator(text, i)) {
                    if (i + 1 < length && text.charAt(i + 1) == c) {
                        ++i;
                    }
                } else {
                    depth--;
                    if (depth == 0) {
                        return i;
                    }
                }
            }
        }
        return -1;
    }
}
