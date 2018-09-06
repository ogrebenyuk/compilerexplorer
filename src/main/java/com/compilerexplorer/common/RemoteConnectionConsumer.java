package com.compilerexplorer.common;

import com.intellij.util.messages.Topic;

public interface RemoteConnectionConsumer {
    Topic<RemoteConnectionConsumer> TOPIC = Topic.create("RemoteConnectionConsumer topic", RemoteConnectionConsumer.class);

    void connected();
}
