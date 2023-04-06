package com.compilerexplorer.gui.tabs;

import org.junit.jupiter.api.Test;

public class WithPreprocessorVersionTabsTest extends BaseTabsTest implements WithPreprocessorVersion {
    @Test
    void verifyPreprocessorVersion() {
        for (Status status : Status.values()) {
            verify(resultWithPreprocessorVersion(status), dataWithPreprocessorVersion(status), stateWithPreprocessorVersion(status));
        }
    }
}
