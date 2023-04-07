package com.compilerexplorer.common;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;

import org.junit.jupiter.api.Test;

@NonNls
class BundleTest {
    @Test
    void verifySubstitutor() {
        verify("", "");
        verify("", "", "a", "b");
        verify("x", "x", "a", "b");
        verify("b", "${a}", "a", "b");
        verify("xby", "x${a}y", "a", "b");
        verify("bb", "${a}${a}", "a", "b");
        verify("bd", "${a}${c}", "a", "b", "c", "d");

        verifyThrows("${a}");
        verifyThrows("${a}", "a");
        verifyThrows("${x}", "b", "c");
    }

    void verify(@NotNull String expected, @NotNull String format, @NotNull String ... args) {
        Assertions.assertEquals(expected, Bundle.Substitutor.replace(format, args));
    }

    void verifyThrows(@NotNull String format, @NotNull String ... args) {
        Assertions.assertThrows(RuntimeException.class, () -> Bundle.Substitutor.replace(format, args));
    }
}