package com.compilerexplorer.gui;

import com.intellij.openapi.editor.markup.EffectType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

@NonNls
public class TerminalColorParserTest {
    @NotNull
    private static final String CSI = TerminalColorParser.CSI_MARKER;
    private static final String SEP = TerminalColorParser.CODES_SEPARATOR;
    private static final char END = TerminalColorParser.END_OF_CODES;

    @Test
    public void verifyParseNoCodes() {
        verifyNoCodes("");
        verifyNoCodes(" ");
        verifyNoCodes("asf afa\nrar\n");
    }

    @Test
    public void verifyParsedText() {
        verifyParsedText(CSI, "");
        verifyParsedText(CSI + END, "");
        verifyParsedText(CSI + SEP, "");
        verifyParsedText(CSI + SEP + END, "");
        verifyParsedText(CSI + SEP + SEP + END, "");
        verifyParsedText(CSI + "30" + END, "");
        verifyParsedText(CSI + SEP + "40" + SEP + "41" + END, "");
        verifyParsedText("a" + CSI + SEP + "40" + SEP + "41" + END, "a");
        verifyParsedText(CSI + END + "a", "a");
        verifyParsedText("a" + CSI + END + "b", "ab");
        verifyParsedText("a" + CSI + SEP + END + "b", "ab");
        verifyParsedText("a" + CSI + SEP + "0" + SEP + END + "b", "ab");
        verifyParsedText("a" + CSI + END + "b" + CSI + END + "c", "abc");
        verifyParsedText("a" + CSI + "1" + SEP + "0" + END + "b", "ab");
        verifyParsedText("a" + CSI + "1" + END + CSI + "0" + END + "b", "ab");
        verifyParsedText("a" + CSI + "1" + END + CSI + END + "b", "ab");
    }

    @Test
    public void verifyIgnoredCodes() {
        verifyCodeIgnored('A');
        verifyCodeIgnored('B');
        verifyCodeIgnored('C');
        verifyCodeIgnored('D');
        verifyCodeIgnored('E');
        verifyCodeIgnored('F');
        verifyCodeIgnored('G');
        verifyCodeIgnored('H');
        verifyCodeIgnored('J');
        verifyCodeIgnored('K');
        verifyCodeIgnored('S');
        verifyCodeIgnored('T');
        verifyCodeIgnored('f');
    }

    @Test
    public void verifyFontType() {
        verifyFontType(1, Font.BOLD);
        verifyFontType(22, Font.PLAIN);
    }

    @Test
    public void verifyEffectType() {
        verifyEffectType(4, EffectType.LINE_UNDERSCORE);
        verifyEffectType(24, null);
    }

    @Test
    public void verifyForegroundColor() {
        verifyForegroundColor(30, TerminalColorParser.COLORS[0]);
        verifyForegroundColor(31, TerminalColorParser.COLORS[1]);
        verifyForegroundColor(32, TerminalColorParser.COLORS[2]);
        verifyForegroundColor(33, TerminalColorParser.COLORS[3]);
        verifyForegroundColor(34, TerminalColorParser.COLORS[4]);
        verifyForegroundColor(35, TerminalColorParser.COLORS[5]);
        verifyForegroundColor(36, TerminalColorParser.COLORS[6]);
        verifyForegroundColor(37, TerminalColorParser.COLORS[7]);
        verifyForegroundColor(90, TerminalColorParser.BRIGHT_COLORS[0]);
        verifyForegroundColor(91, TerminalColorParser.BRIGHT_COLORS[1]);
        verifyForegroundColor(92, TerminalColorParser.BRIGHT_COLORS[2]);
        verifyForegroundColor(93, TerminalColorParser.BRIGHT_COLORS[3]);
        verifyForegroundColor(94, TerminalColorParser.BRIGHT_COLORS[4]);
        verifyForegroundColor(95, TerminalColorParser.BRIGHT_COLORS[5]);
        verifyForegroundColor(96, TerminalColorParser.BRIGHT_COLORS[6]);
        verifyForegroundColor(97, TerminalColorParser.BRIGHT_COLORS[7]);
    }

    @Test
    public void verifyBackgroundColor() {
        verifyBackgroundColor(40, TerminalColorParser.COLORS[0]);
        verifyBackgroundColor(41, TerminalColorParser.COLORS[1]);
        verifyBackgroundColor(42, TerminalColorParser.COLORS[2]);
        verifyBackgroundColor(43, TerminalColorParser.COLORS[3]);
        verifyBackgroundColor(44, TerminalColorParser.COLORS[4]);
        verifyBackgroundColor(45, TerminalColorParser.COLORS[5]);
        verifyBackgroundColor(46, TerminalColorParser.COLORS[6]);
        verifyBackgroundColor(47, TerminalColorParser.COLORS[7]);
        verifyBackgroundColor(100, TerminalColorParser.BRIGHT_COLORS[0]);
        verifyBackgroundColor(101, TerminalColorParser.BRIGHT_COLORS[1]);
        verifyBackgroundColor(102, TerminalColorParser.BRIGHT_COLORS[2]);
        verifyBackgroundColor(103, TerminalColorParser.BRIGHT_COLORS[3]);
        verifyBackgroundColor(104, TerminalColorParser.BRIGHT_COLORS[4]);
        verifyBackgroundColor(105, TerminalColorParser.BRIGHT_COLORS[5]);
        verifyBackgroundColor(106, TerminalColorParser.BRIGHT_COLORS[6]);
        verifyBackgroundColor(107, TerminalColorParser.BRIGHT_COLORS[7]);
    }

    @Test
    public void verifyForegroundColor256() {
        verifyForegroundColor256(0, TerminalColorParser.COLORS[0]);
        verifyForegroundColor256(1, TerminalColorParser.COLORS[1]);
        verifyForegroundColor256(2, TerminalColorParser.COLORS[2]);
        verifyForegroundColor256(3, TerminalColorParser.COLORS[3]);
        verifyForegroundColor256(4, TerminalColorParser.COLORS[4]);
        verifyForegroundColor256(5, TerminalColorParser.COLORS[5]);
        verifyForegroundColor256(7, TerminalColorParser.COLORS[7]);
        verifyForegroundColor256(8, TerminalColorParser.BRIGHT_COLORS[0]);
        verifyForegroundColor256(9, TerminalColorParser.BRIGHT_COLORS[1]);
        verifyForegroundColor256(10, TerminalColorParser.BRIGHT_COLORS[2]);
        verifyForegroundColor256(11, TerminalColorParser.BRIGHT_COLORS[3]);
        verifyForegroundColor256(12, TerminalColorParser.BRIGHT_COLORS[4]);
        verifyForegroundColor256(13, TerminalColorParser.BRIGHT_COLORS[5]);
        verifyForegroundColor256(14, TerminalColorParser.BRIGHT_COLORS[6]);
        verifyForegroundColor256(15, TerminalColorParser.BRIGHT_COLORS[7]);

        verifyForegroundColor256(16, color(0, 0, 0));
        verifyForegroundColor256(231, color(255, 255, 255));

        verifyForegroundColor256(232, color(0, 0, 0));
        verifyForegroundColor256(255, color(253, 253, 253));
    }

    @Test
    public void verifyBackgroundColor256() {
        verifyBackgroundColor256(0, TerminalColorParser.COLORS[0]);
        verifyBackgroundColor256(1, TerminalColorParser.COLORS[1]);
        verifyBackgroundColor256(2, TerminalColorParser.COLORS[2]);
        verifyBackgroundColor256(3, TerminalColorParser.COLORS[3]);
        verifyBackgroundColor256(4, TerminalColorParser.COLORS[4]);
        verifyBackgroundColor256(5, TerminalColorParser.COLORS[5]);
        verifyBackgroundColor256(7, TerminalColorParser.COLORS[7]);
        verifyBackgroundColor256(8, TerminalColorParser.BRIGHT_COLORS[0]);
        verifyBackgroundColor256(9, TerminalColorParser.BRIGHT_COLORS[1]);
        verifyBackgroundColor256(10, TerminalColorParser.BRIGHT_COLORS[2]);
        verifyBackgroundColor256(11, TerminalColorParser.BRIGHT_COLORS[3]);
        verifyBackgroundColor256(12, TerminalColorParser.BRIGHT_COLORS[4]);
        verifyBackgroundColor256(13, TerminalColorParser.BRIGHT_COLORS[5]);
        verifyBackgroundColor256(14, TerminalColorParser.BRIGHT_COLORS[6]);
        verifyBackgroundColor256(15, TerminalColorParser.BRIGHT_COLORS[7]);

        verifyBackgroundColor256(16, color(0, 0, 0));
        verifyBackgroundColor256(231, color(255, 255, 255));

        verifyBackgroundColor256(232, color(0, 0, 0));
        verifyBackgroundColor256(255, color(253, 253, 253));
    }

    @Test
    public void verifyForegroundColor24() {
        verifyForegroundColor24(0, 0, 0, color(0, 0, 0));
        verifyForegroundColor24(12, 23, 34, color(12, 23, 34));
        verifyForegroundColor24(255, 255, 255, color(255, 255, 255));
    }

    @Test
    public void verifyBackgroundColor24() {
        verifyBackgroundColor24(0, 0, 0, color(0, 0, 0));
        verifyBackgroundColor24(12, 23, 34, color(12, 23, 34));
        verifyBackgroundColor24(255, 255, 255, color(255, 255, 255));
    }

    private static void verifyFontType(int code, int expectedFontType) {
        List<TerminalColorParser.HighlightedRange> ranges = create();
        @NonNls
        String parsed = TerminalColorParser.parse("a" + CSI + code + END + "b" + CSI + END + "c", ranges);
        Assertions.assertEquals("abc", parsed);
        Assertions.assertEquals(1, ranges.size());
        Assertions.assertEquals(1, ranges.get(0).startOffset);
        Assertions.assertEquals(2, ranges.get(0).endOffset);
        Assertions.assertEquals(expectedFontType, ranges.get(0).textAttributes.getFontType());
    }

    private static void verifyEffectType(int code, @Nullable EffectType expectedEffectType) {
        List<TerminalColorParser.HighlightedRange> ranges = create();
        @NonNls
        String parsed = TerminalColorParser.parse("a" + CSI + code + END + "b" + CSI + END + "c", ranges);
        Assertions.assertEquals("abc", parsed);
        Assertions.assertEquals(1, ranges.size());
        Assertions.assertEquals(1, ranges.get(0).startOffset);
        Assertions.assertEquals(2, ranges.get(0).endOffset);
        Assertions.assertEquals(expectedEffectType, ranges.get(0).textAttributes.getEffectType());
    }

    private static void verifyForegroundColor(int code, @NotNull Color expectedColor) {
        List<TerminalColorParser.HighlightedRange> ranges = create();
        @NonNls
        String parsed = TerminalColorParser.parse("a" + CSI + code + END + "b" + CSI + END + "c", ranges);
        Assertions.assertEquals("abc", parsed);
        Assertions.assertEquals(1, ranges.size());
        Assertions.assertEquals(1, ranges.get(0).startOffset);
        Assertions.assertEquals(2, ranges.get(0).endOffset);
        Assertions.assertEquals(expectedColor, ranges.get(0).textAttributes.getForegroundColor());
    }

    private static void verifyBackgroundColor(int code, @NotNull Color expectedColor) {
        List<TerminalColorParser.HighlightedRange> ranges = create();
        @NonNls
        String parsed = TerminalColorParser.parse("a" + CSI + code + END + "b" + CSI + END + "c", ranges);
        Assertions.assertEquals("abc", parsed);
        Assertions.assertEquals(1, ranges.size());
        Assertions.assertEquals(1, ranges.get(0).startOffset);
        Assertions.assertEquals(2, ranges.get(0).endOffset);
        Assertions.assertEquals(expectedColor, ranges.get(0).textAttributes.getBackgroundColor());
    }

    private static void verifyForegroundColor256(int code, @NotNull Color expectedColor) {
        List<TerminalColorParser.HighlightedRange> ranges = create();
        @NonNls
        String parsed = TerminalColorParser.parse("a" + CSI + "38" + SEP + "5" + SEP + code + END + "b" + CSI + END + "c", ranges);
        Assertions.assertEquals("abc", parsed);
        Assertions.assertEquals(1, ranges.size());
        Assertions.assertEquals(1, ranges.get(0).startOffset);
        Assertions.assertEquals(2, ranges.get(0).endOffset);
        Assertions.assertEquals(expectedColor, ranges.get(0).textAttributes.getForegroundColor());
    }

    private static void verifyBackgroundColor256(int code, @NotNull Color expectedColor) {
        List<TerminalColorParser.HighlightedRange> ranges = create();
        @NonNls
        String parsed = TerminalColorParser.parse("a" + CSI + "48" + SEP + "5" + SEP + code + END + "b" + CSI + END + "c", ranges);
        Assertions.assertEquals("abc", parsed);
        Assertions.assertEquals(1, ranges.size());
        Assertions.assertEquals(1, ranges.get(0).startOffset);
        Assertions.assertEquals(2, ranges.get(0).endOffset);
        Assertions.assertEquals(expectedColor, ranges.get(0).textAttributes.getBackgroundColor());
    }

    private static void verifyForegroundColor24(int r, int g, int b, @NotNull Color expectedColor) {
        List<TerminalColorParser.HighlightedRange> ranges = create();
        @NonNls
        String parsed = TerminalColorParser.parse("a" + CSI + "38" + SEP + "2" + SEP + r + SEP + g + SEP + b + END + "b" + CSI + END + "c", ranges);
        Assertions.assertEquals("abc", parsed);
        Assertions.assertEquals(1, ranges.size());
        Assertions.assertEquals(1, ranges.get(0).startOffset);
        Assertions.assertEquals(2, ranges.get(0).endOffset);
        Assertions.assertEquals(expectedColor, ranges.get(0).textAttributes.getForegroundColor());
    }

    private static void verifyBackgroundColor24(int r, int g, int b, @NotNull Color expectedColor) {
        List<TerminalColorParser.HighlightedRange> ranges = create();
        @NonNls
        String parsed = TerminalColorParser.parse("a" + CSI + "48" + SEP + "2" + SEP + r + SEP + g + SEP + b + END + "b" + CSI + END + "c", ranges);
        Assertions.assertEquals("abc", parsed);
        Assertions.assertEquals(1, ranges.size());
        Assertions.assertEquals(1, ranges.get(0).startOffset);
        Assertions.assertEquals(2, ranges.get(0).endOffset);
        Assertions.assertEquals(expectedColor, ranges.get(0).textAttributes.getBackgroundColor());
    }

    private static void verifyCodeIgnored(char code) {
        verifyParsedText(CSI + code, "");
        verifyParsedText("a" + CSI + code, "a");
        verifyParsedText(CSI + code + "a", "a");
        verifyParsedText("a" + CSI + code + "b", "ab");
    }

    private static void verifyNoCodes(@NonNls @NotNull String text) {
        verifyParsedText(text, text);
    }

    private static void verifyParsedText(@NonNls @NotNull String text, @NonNls @NotNull String expectedParsed) {
        List<TerminalColorParser.HighlightedRange> ranges = create();
        String parsed = TerminalColorParser.parse(text, ranges);
        Assertions.assertEquals(expectedParsed, parsed);
        Assertions.assertEquals(0, ranges.size());
    }

    @NotNull
    private static List<TerminalColorParser.HighlightedRange> create() {
        return new ArrayList<>();
    }

    @NotNull
    private static Color color(int r, int g, int b) {
        return TerminalColorParser.color(r, g, b);
    }
}