package com.compilerexplorer.common.component;

import com.intellij.openapi.application.ApplicationManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class BaseLinkedComponent implements CEComponent {
    @Nullable
    private final CEComponent nextComponent;

    public BaseLinkedComponent() {
        nextComponent = null;
    }

    public BaseLinkedComponent(@NotNull CEComponent nextComponent_) {
        nextComponent = nextComponent_;
    }

    @Override
    public void refresh(@NotNull DataHolder data) {
        refreshNext(data);
    }

    protected void refreshNext(@NotNull DataHolder data) {
        if (nextComponent != null) {
            ApplicationManager.getApplication().invokeLater(() -> nextComponent.refresh(data));
        }
    }
}
