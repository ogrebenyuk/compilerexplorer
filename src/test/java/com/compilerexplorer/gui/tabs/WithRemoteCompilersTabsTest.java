package com.compilerexplorer.gui.tabs;

import org.junit.jupiter.api.Test;

public class WithRemoteCompilersTabsTest extends BaseTabsTest implements WithRemoteCompilers {
    @Test
    void verifyRemoteCompilers() {
        for (Status status : Status.values()) {
            verify(resultWithRemoteCompilers(status), dataWithRemoteCompilers(status), stateWithRemoteCompilers(status));
        }
    }
}
