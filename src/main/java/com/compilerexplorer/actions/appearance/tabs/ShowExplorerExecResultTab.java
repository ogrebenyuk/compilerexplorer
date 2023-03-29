package com.compilerexplorer.actions.appearance.tabs;

import com.compilerexplorer.common.Tabs;
import com.intellij.openapi.actionSystem.AnActionEvent;
import org.jetbrains.annotations.NotNull;

public class ShowExplorerExecResultTab extends BaseShowTab {
    public ShowExplorerExecResultTab() {
        super(Tabs.EXPLORER_EXEC_RESULT);
    }

    @Override
    public void update(@NotNull final AnActionEvent event) {
        super.update(event);
        withFilters(event, filters -> setEnabled(event, isEnabled(event) && filters.getExecute()));
    }
}
