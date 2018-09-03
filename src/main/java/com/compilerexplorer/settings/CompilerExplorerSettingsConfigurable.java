package com.compilerexplorer.settings;

import com.intellij.openapi.options.Configurable;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class CompilerExplorerSettingsConfigurable implements Configurable {
    @NotNull
    private final CompilerExplorerSettingsProvider provider;
    private CompilerExplorerSettingsGui form;

    CompilerExplorerSettingsConfigurable(@NotNull CompilerExplorerSettingsProvider provider_) {
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
            form = new CompilerExplorerSettingsGui(provider.getProject());
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
