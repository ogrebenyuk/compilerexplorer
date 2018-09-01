package com.compilerexplorer.base;

import org.jetbrains.annotations.NotNull;

public interface Explorer {
    void setTextConsumer(@NotNull TextConsumer consumer);

    void refresh();
}
