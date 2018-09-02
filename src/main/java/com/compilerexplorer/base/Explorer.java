package com.compilerexplorer.base;

import com.compilerexplorer.base.handlers.TextConsumer;
import org.jetbrains.annotations.NotNull;

public interface Explorer {
    void setTextConsumer(@NotNull TextConsumer consumer);

    void refresh();
}
