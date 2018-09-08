package com.compilerexplorer.common.datamodel;

import org.jetbrains.annotations.NotNull;

public interface SourceRemoteMatchedConsumer {
    void setSourceRemoteMatched(@NotNull SourceRemoteMatched sourceRemoteMatched);
    void clearSourceRemoteMatched(@NotNull String reason);
}
