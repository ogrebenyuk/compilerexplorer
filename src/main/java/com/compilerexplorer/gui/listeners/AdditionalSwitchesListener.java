package com.compilerexplorer.gui.listeners;

import org.jetbrains.annotations.NotNull;

import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.JTextComponent;
import java.util.function.Consumer;

public class AdditionalSwitchesListener implements DocumentListener {
    @NotNull
    private final JTextComponent component;
    @NotNull
    private final Consumer<String> consumer;

    public AdditionalSwitchesListener(@NotNull JTextComponent component_, @NotNull Consumer<String> consumer_) {
        component = component_;
        consumer = consumer_;
    }

    @Override
    public void changedUpdate(DocumentEvent e) {
        // empty
    }

    @Override
    public void removeUpdate(DocumentEvent e) {
        update();
    }

    @Override
    public void insertUpdate(DocumentEvent e) {
        update();
    }

    private void update() {
        consumer.accept(component.getText());
    }
}
