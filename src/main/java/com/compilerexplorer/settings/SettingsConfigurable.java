package com.compilerexplorer.settings;

import com.compilerexplorer.common.SettingsProvider;
import com.compilerexplorer.settings.gui.SettingsGui;
import com.intellij.openapi.options.Configurable;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class SettingsConfigurable implements Configurable {
    @NotNull
    private final SettingsProvider provider;
    private SettingsGui form;

    SettingsConfigurable(@NotNull SettingsProvider provider_) {
        provider = provider_;
    }

    @Override
    public String getDisplayName() {
        return "Compiler Explorer";
    }

    @Override
    @NotNull
    public JComponent createComponent() {
        if (form == null) {
            form = new SettingsGui(provider.getProject());
            reset();
        }
        return form.getContent();
    }

    @Override
    public boolean isModified() {
        return !provider.getState().equals(form.getState());
    }

    @Override
    public void apply() {
        provider.loadState(form.getState());
    }

    @Override
    public void reset() {
        form.loadState(provider.getState());
    }

    @Override
    public void disposeUIResources() {
        form = null;
    }

}
