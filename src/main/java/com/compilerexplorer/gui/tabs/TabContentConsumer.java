package com.compilerexplorer.gui.tabs;

import com.intellij.openapi.fileTypes.FileType;
import com.intellij.util.Producer;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public interface TabContentConsumer {
    void accept(boolean enabled, boolean error, @NotNull FileType filetype, @NonNls @NotNull String defaultExtension, @NotNull Producer<TabContent> contentProducer);
}
