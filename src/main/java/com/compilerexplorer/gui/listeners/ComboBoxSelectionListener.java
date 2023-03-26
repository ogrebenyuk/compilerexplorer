package com.compilerexplorer.gui.listeners;

import com.intellij.openapi.ui.ComboBox;
import org.jetbrains.annotations.NotNull;

import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.function.Consumer;

public class ComboBoxSelectionListener<T> implements ItemListener {
    @NotNull
    private final ComboBox<T> comboBox;
    @NotNull
    private final Consumer<T> consumer;

    public ComboBoxSelectionListener(@NotNull ComboBox<T> comboBox_, @NotNull Consumer<T> consumer_) {
        comboBox = comboBox_;
        consumer = consumer_;
    }

    @Override
    public void itemStateChanged(@NotNull ItemEvent event) {
        if (event.getStateChange() == ItemEvent.SELECTED) {
            consumer.accept(comboBox.getItemAt(comboBox.getSelectedIndex()));
        }
    }
}
