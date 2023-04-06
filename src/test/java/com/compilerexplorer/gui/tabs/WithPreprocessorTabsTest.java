package com.compilerexplorer.gui.tabs;

import org.junit.jupiter.api.Test;

public class WithPreprocessorTabsTest extends BaseTabsTest implements WithPreprocessor {
    @Test
    void verifyPreprocessorVersion() {
        for (Status status : Status.values()) {
            verify(resultWithPreprocessor(status), dataWithPreprocessor(status), stateWithPreprocessor(status));
        }
    }
}
