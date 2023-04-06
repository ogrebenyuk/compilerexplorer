package com.compilerexplorer.gui.tabs;

import org.junit.jupiter.api.Test;

public class WithMatchTabsTest extends BaseTabsTest implements WithMatch {
    @Test
    void verifyPreprocessorVersion() {
        for (Status status : Status.values()) {
            verify(resultWithMatch(status), dataWithMatch(status), stateWithMatch(status));
        }
    }
}
