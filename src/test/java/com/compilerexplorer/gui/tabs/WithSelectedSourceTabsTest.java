package com.compilerexplorer.gui.tabs;

import org.junit.jupiter.api.Test;

public class WithSelectedSourceTabsTest extends BaseTabsTest implements WithSelectedSource {
    @Test
    void verifyWithSelectedSource() {
        for (Status status : Status.values()) {
            verify(resultWithSelectedSource(status), dataWithSelectedSource(status), stateWithSelectedSource(status));
        }
    }
}
