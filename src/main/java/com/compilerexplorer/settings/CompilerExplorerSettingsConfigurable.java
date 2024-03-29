package com.compilerexplorer.settings;

import com.compilerexplorer.common.Bundle;
import com.compilerexplorer.common.CompilerExplorerSettingsProvider;
import com.compilerexplorer.settings.gui.SettingsGui;
import com.intellij.openapi.options.Configurable;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class CompilerExplorerSettingsConfigurable implements Configurable {
    @NotNull
    private final CompilerExplorerSettingsProvider provider;
    @Nullable
    private SettingsGui form;
    private boolean showUrlHistoryOnStart;

    CompilerExplorerSettingsConfigurable(@NotNull Project project_) {
        provider = CompilerExplorerSettingsProvider.getInstance(project_);
    }

    public void showUrlHistoryOnStart(boolean show) {
        showUrlHistoryOnStart = show;
    }

    @Override
    @NotNull
    public String getDisplayName() {
        return Bundle.get("compilerexplorer.CompilerExplorerSettingsConfigurable.DisplayName");
    }

    @Override
    @NotNull
    public JComponent createComponent() {
        if (form == null) {
            form = new SettingsGui(provider.getProject(), showUrlHistoryOnStart);
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
            form.dispose();
        }
        form = null;
    }

}
