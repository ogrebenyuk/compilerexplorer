package com.compilerexplorer.compiler;

import com.compilerexplorer.common.CompilerExplorerSettingsProvider;
import com.compilerexplorer.common.component.BaseComponent;
import com.compilerexplorer.common.component.CEComponent;
import com.compilerexplorer.common.component.DataHolder;
import com.compilerexplorer.datamodel.SelectedSource;
import com.compilerexplorer.datamodel.SourceRemoteMatched;
import com.compilerexplorer.datamodel.state.LocalCompilerPath;
import com.compilerexplorer.datamodel.state.SettingsState;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class SourceRemoteMatchSaver extends BaseComponent {
    @NonNls
    private static final Logger LOG = Logger.getInstance(SourceRemoteMatchSaver.class);

    @NotNull
    private final Project project;

    public SourceRemoteMatchSaver(@NotNull CEComponent nextComponent, @NotNull Project project_) {
        super(nextComponent);
        LOG.debug("created");

        project = project_;
    }

    @Override
    protected void doClear(@NotNull DataHolder data) {
        LOG.debug("doClear");
    }

    @Override
    protected void doRefresh(@NotNull DataHolder data) {
        LOG.debug("doRefresh");
        SettingsState state = CompilerExplorerSettingsProvider.getInstance(project).getState();
        data.get(SourceRemoteMatched.SELECTED_KEY).ifPresentOrElse(sourceRemoteMatched -> data.get(SelectedSource.KEY).ifPresentOrElse(selectedSource -> {
            LOG.debug("saving " + selectedSource.getSelectedSource().compilerPath + " -> " + sourceRemoteMatched.getMatches().getChosenMatch().getRemoteCompilerInfo().getName());
            state.addToCompilerMatches(new LocalCompilerPath(selectedSource.getSelectedSource().compilerPath), sourceRemoteMatched.getMatches());
        }, () -> LOG.debug("cannot find input: source")), () -> LOG.debug("cannot find input: match"));
    }
}
