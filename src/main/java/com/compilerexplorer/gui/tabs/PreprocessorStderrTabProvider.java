package com.compilerexplorer.gui.tabs;

import com.compilerexplorer.common.Tabs;
import com.compilerexplorer.datamodel.state.SettingsState;
import org.jetbrains.annotations.NotNull;

public class PreprocessorStderrTabProvider extends BasePreprocessorTabProvider {
    public PreprocessorStderrTabProvider(@NotNull SettingsState state) {
        super(state, Tabs.PREPROCESSOR_STDERR, "compilerexplorer.ShowPreprocessorStderrTab", false, output -> output != null ? output.getStderr() : null);
    }
}
