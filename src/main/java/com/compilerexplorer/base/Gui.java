package com.compilerexplorer.base;

import com.compilerexplorer.base.handlers.ClickHandler;
import org.jetbrains.annotations.NotNull;

import javax.swing.JComponent;

public interface Gui {
    void setRefreshClickHandler(@NotNull ClickHandler handler);

    @NotNull
    JComponent getContent();

    void setMainText(@NotNull String text);
}
