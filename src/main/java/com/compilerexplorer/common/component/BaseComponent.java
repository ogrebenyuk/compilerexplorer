package com.compilerexplorer.common.component;

import org.jetbrains.annotations.NotNull;

public abstract class BaseComponent extends BaseLinkedComponent {
    public BaseComponent() {
        super();
    }

    public BaseComponent(@NotNull CEComponent nextComponent) {
        super(nextComponent);
    }

    @Override
    public void refresh(@NotNull DataHolder data) {
        doClear(data);
        if (ResetFlag.in(data)) {
            doReset(data);
        }
        doRefresh(data);
        super.refresh(data);
    }

    protected abstract void doClear(@NotNull DataHolder data);

    protected void doReset(@NotNull DataHolder data) {
        // empty
    }

    protected abstract void doRefresh(@NotNull DataHolder data);
}
