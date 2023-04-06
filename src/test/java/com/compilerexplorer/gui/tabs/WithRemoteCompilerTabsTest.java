package com.compilerexplorer.gui.tabs;

import org.junit.jupiter.api.Test;

public class WithRemoteCompilerTabsTest extends BaseTabsTest implements WithRemoteCompiler {
    @Test
    void verifyRemoteCompiler() {
        for (Status status : Status.values()) {
            verify(resultWithRemoteCompiler(status), dataWithRemoteCompiler(status), stateWithRemoteCompiler(status));
        }
    }
}
