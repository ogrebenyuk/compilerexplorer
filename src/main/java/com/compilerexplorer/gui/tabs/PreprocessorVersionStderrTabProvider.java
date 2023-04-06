package com.compilerexplorer.gui.tabs;

import com.compilerexplorer.common.Tabs;
import com.compilerexplorer.datamodel.state.SettingsState;
import org.jetbrains.annotations.NotNull;

public class PreprocessorVersionStderrTabProvider extends BasePreprocessorVersionTabProvider {
    public PreprocessorVersionStderrTabProvider(@NotNull SettingsState state) {
        super(state, Tabs.PREPROCESSOR_VERSION_STDERR, "compilerexplorer.ShowPreprocessorVersionStderrTab", false, output -> output != null ? output.getStderr() : "");
    }
}
