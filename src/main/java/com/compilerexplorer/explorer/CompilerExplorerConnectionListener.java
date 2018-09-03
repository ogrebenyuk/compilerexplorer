package com.compilerexplorer.explorer;

import com.compilerexplorer.common.CompilerExplorerConnectionConsumer;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class CompilerExplorerConnectionListener {
    public CompilerExplorerConnectionListener(@NotNull Project project, @NotNull CompilerExplorerConnectionConsumer compilerExplorerConnectionConsumer) {
        project.getMessageBus().connect().subscribe(CompilerExplorerConnectionConsumer.TOPIC, compilerExplorerConnectionConsumer);
    }
}
