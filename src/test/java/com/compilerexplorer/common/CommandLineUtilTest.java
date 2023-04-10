package com.compilerexplorer.common;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

class CommandLineUtilTest {
    @Test
    public void verifyCommandLineParsing() {
        verifyCommandLineParsing("");
        verifyCommandLineParsing(" ");
        verifyCommandLineParsing("   ");

        verifyCommandLineParsing("a", "a");
        verifyCommandLineParsing("abc xyz", "abc", "xyz");

        verifyCommandLineParsing("\"a\"", "a");
        verifyCommandLineParsing("\"a\" b", "a", "b");
        verifyCommandLineParsing("\"a\" \"b\"", "a", "b");
        verifyCommandLineParsing("\"a b\"", "a b");
        verifyCommandLineParsing("\"a b c \" \" \" \" x\" \" y \"", "a b c ", " ", " x", " y ");
    }

    @Test
    public void verifyCommandLineForming() {
        verifyCommandLineForming("");
        verifyCommandLineForming("\"\"", "");
        verifyCommandLineForming("\" \"", " ");

        verifyCommandLineForming("a", "a");
        verifyCommandLineForming("abc xyz", "abc", "xyz");

        verifyCommandLineForming("\"a b\"", "a b");
        verifyCommandLineForming("\"a b c \" \" \" \" x\" \" y \"", "a b c ", " ", " x", " y ");
    }

    private void verifyCommandLineParsing(@NonNls @NotNull String commandLine, @NonNls @NotNull String ... expectedParsedOptions) {
        List<String> parsed = CommandLineUtil.parseCommandLine(commandLine);
        Assertions.assertEquals(Arrays.stream(expectedParsedOptions).toList(), parsed);
    }

    private void verifyCommandLineForming(@NonNls @NotNull String expectedCommandLine, @NonNls @NotNull String ... options) {
        String formed = CommandLineUtil.formCommandLine(List.of(options));
        Assertions.assertEquals(expectedCommandLine, formed);
    }
}