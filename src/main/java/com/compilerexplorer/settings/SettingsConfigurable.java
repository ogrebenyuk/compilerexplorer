package com.compilerexplorer.settings;

import com.compilerexplorer.common.SettingsProvider;
import com.compilerexplorer.settings.gui.SettingsGui;
import com.intellij.openapi.options.Configurable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class SettingsConfigurable implements Configurable {
    @NotNull
    private final SettingsProvider provider;
    @Nullable
    private SettingsGui form;

    SettingsConfigurable(@NotNull SettingsProvider provider_) {
        provider = provider_;
    }

    @Override
    @NotNull
    public String getDisplayName() {
        return "Compiler Explorer";
    }

    @Override
    @NotNull
    public JComponent createComponent() {
        if (form == null) {
            form = new SettingsGui(provider.getProject());
        }
        reset();
        return form.getContent();
    }

    @Override
    public boolean isModified() {
        return form != null && !provider.getState().equals(form.getState());
    }

    @Override
    public void apply() {
        if (form != null) {
            provider.copyFrom(form.getState());
        }
    }

    @Override
    public void reset() {
        if (form != null) {
            form.copyFrom(provider.getState());
        }
    }

    @Override
    public void disposeUIResources() {
        if (form != null) {
            form.reset();
        }
        form = null;
    }

}
