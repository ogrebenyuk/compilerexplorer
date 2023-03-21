package com.compilerexplorer.compiler;

import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;


class SourceRemoteMatchProducerTest {
    @Test
    void testExactMatch() {
        verifyExactMatch("0", "0");
        verifyExactMatch("0.0", "0.0");
        verifyExactMatch("0.0.0", "0.0.0");
    }

    @Test
    void testMinorMismatch() {
        verifyMinorMismatch("0.1", "0.0");
        verifyMinorMismatch("0.0.1", "0.0.0");
        verifyMinorMismatch("4.6.4", "4.6.0");
    }

    @Test
    void testNoMatch() {
        verifyNoMatch("", "0");
        verifyNoMatch("", "0.0");
        verifyNoMatch("", "0.0.0");

        verifyNoMatch("1", "0");
        verifyNoMatch("1.0", "0.0");
        verifyNoMatch("0.1.0", "0.0.0");
    }

    private static void verifyExactMatch(@NotNull String remoteName, @NotNull String localVersion) {
        verifyMatch(remoteName, localVersion, false, true);
    }

    private static void verifyMinorMismatch(@NotNull String remoteName, @NotNull String localVersion) {
        verifyMatch(remoteName, localVersion, false, false);
        verifyMatch(remoteName, localVersion, true, true);
    }

    private static void verifyNoMatch(@NotNull String remoteName, @NotNull String localVersion) {
        verifyMatch(remoteName, localVersion, false, false);
        verifyMatch(remoteName, localVersion, true, false);
    }

    private static void verifyMatch(@NotNull String remoteName, @NotNull String localVersion, boolean tryMinorMismatch, boolean expectedMatch) {
        Assertions.assertFalse(SourceRemoteMatchProducer.versionMatches(remoteName, localVersion, tryMinorMismatch));
        Assertions.assertEquals(SourceRemoteMatchProducer.versionMatches(" " + remoteName, localVersion, tryMinorMismatch), expectedMatch);
        Assertions.assertEquals(SourceRemoteMatchProducer.versionMatches(" " + remoteName + " ", localVersion, tryMinorMismatch), expectedMatch);
    }
}