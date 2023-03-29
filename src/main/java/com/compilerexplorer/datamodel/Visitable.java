package com.compilerexplorer.datamodel;

import org.jetbrains.annotations.NotNull;

public interface Visitable {
    void accept(@NotNull Visitor visitor);
}
