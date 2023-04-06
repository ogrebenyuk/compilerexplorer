package com.compilerexplorer.gui.tabs;

import org.junit.jupiter.api.Test;

public class WithSourcesTabsTest extends BaseTabsTest implements WithSources {
    @Test
    void verifyWithSources() {
        verify(resultWithSources(), dataWithSources(), stateWithSources());
    }
}
