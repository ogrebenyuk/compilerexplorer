package com.compilerexplorer.common;

import com.intellij.util.messages.Topic;

public interface CompilerExplorerConnectionConsumer {
    Topic<CompilerExplorerConnectionConsumer> TOPIC = Topic.create("CompilerExplorerConnectionConsumer topic", CompilerExplorerConnectionConsumer.class);

    void connected();
}
