package com.compilerexplorer.gui;

import com.compilerexplorer.common.Bundle;
import com.compilerexplorer.common.TextChangeListener;
import com.compilerexplorer.common.TooltipUtil;
import com.intellij.ui.components.JBTextField;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.function.Consumer;

public class AdditionalSwitchesGui {
    @NotNull
    private final JBTextField textField;

    public AdditionalSwitchesGui(@NotNull String initialText, @NotNull Consumer<String> consumer) {
        textField = new JBTextField(initialText);
        textField.setToolTipText(TooltipUtil.prettify(Bundle.get("compilerexplorer.AdditionalSwitchesGui.Tooltip")));
        textField.getDocument().addDocumentListener(new TextChangeListener(textField, consumer));
    }

    @NotNull
    public Component getComponent() {
        return textField;
    }
}
