package com.compilerexplorer.common.component;

import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class BaseRefreshableComponent extends BaseComponent {
    @NonNls
    private static final Logger LOG = Logger.getInstance(BaseRefreshableComponent.class);

    @Nullable
    private DataHolder lastData;

    public BaseRefreshableComponent() {
        super();
    }

    public BaseRefreshableComponent(@NotNull CEComponent nextComponent) {
        super(nextComponent);
    }

    @Nullable
    protected DataHolder getLastData() {
        return lastData;
    }

    @Override
    public void refresh(@NotNull DataHolder data) {
        lastData = data;
        super.refresh(data);
    }

    public void refresh(boolean reset) {
        if (lastData != null) {
            LOG.debug("refresh " + reset);
            refresh(ResetFlag.with(ResetLevel.with(lastData, ResetLevel.NONE), reset));
        } else {
            LOG.debug("cannot refresh because no saved data");
        }
    }
}
