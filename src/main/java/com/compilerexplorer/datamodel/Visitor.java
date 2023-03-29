package com.compilerexplorer.datamodel;

import org.jetbrains.annotations.NotNull;

public interface Visitor {
    void visit(@NotNull Visitable visitable);

    default void unexpected(@NotNull Visitable visitable) {
        throw new RuntimeException("Unexpected visitable " + visitable);
    }
}
