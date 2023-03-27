package com.compilerexplorer.settings;

import com.compilerexplorer.common.CompilerExplorerSettingsProvider;
import com.compilerexplorer.common.Constants;
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

    CompilerExplorerSettingsConfigurable(@NotNull Project project_) {
        provider = CompilerExplorerSettingsProvider.getInstance(project_);
    }

    @Override
    @NotNull
    public String getDisplayName() {
        return Constants.PROJECT_TITLE;
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
            form.dispose();
        }
        form = null;
    }

}
