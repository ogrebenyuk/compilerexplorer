package com.compilerexplorer.gui.tabs;

import com.compilerexplorer.common.Tabs;
import com.compilerexplorer.datamodel.state.SettingsState;
import org.jetbrains.annotations.NotNull;

public class PreprocessorVersionStdoutTabProvider extends BasePreprocessorVersionTabProvider {
    public PreprocessorVersionStdoutTabProvider(@NotNull SettingsState state) {
        super(state, Tabs.PREPROCESSOR_VERSION_STDOUT, "compilerexplorer.ShowPreprocessorVersionStdoutTab", false, output -> output != null ? output.getStdout() : "");
    }
}
