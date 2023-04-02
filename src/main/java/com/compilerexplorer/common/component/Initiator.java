package com.compilerexplorer.common.component;

import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;

public class Initiator extends BaseLinkedComponent implements Refreshable {
    private static final Logger LOG = Logger.getInstance(Initiator.class);

    @NotNull
    private DataHolder createData() {
        return new DataHolder();
    }

    public Initiator(@NotNull CEComponent nextComponent) {
        super(nextComponent);
        LOG.debug("created");
    }

    @Override
    public void refresh(boolean reset) {
        refresh(reset, ResetLevel.NONE);
    }

    public void refresh(boolean reset, @NotNull ResetLevel resetLevel) {
        LOG.debug("refresh " + reset + " " + resetLevel);
        refresh(ResetFlag.with(ResetLevel.with(createData(), resetLevel), reset));
    }
}
