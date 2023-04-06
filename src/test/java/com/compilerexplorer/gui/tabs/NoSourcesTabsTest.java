package com.compilerexplorer.gui.tabs;

import org.junit.jupiter.api.Test;

public class NoSourcesTabsTest extends BaseTabsTest implements NoSources {
    @Test
    void verifyNoSources() {
        verify(resultWithNoSources(), dataWithNoSources(), stateWithNoSources());
    }

    @Test
    void verifyEmptySources() {
        verify(resultWithNoSources(), dataWithEmptySources(), stateWithNoSources());
    }
}
