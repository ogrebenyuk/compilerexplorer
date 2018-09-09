package com.compilerexplorer.common.datamodel.state;

import com.intellij.util.messages.Topic;

public interface StateConsumer {
    Topic<StateConsumer> TOPIC = Topic.create("StateConsumer topic", StateConsumer.class);

    void stateChanged();

    void reset();
}
