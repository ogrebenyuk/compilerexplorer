package com.compilerexplorer.gui.tabs;

import com.compilerexplorer.common.Tabs;
import com.compilerexplorer.datamodel.state.SettingsState;
import org.jetbrains.annotations.NotNull;

public class PreprocessorStdoutTabProvider extends BasePreprocessorTabProvider {
    public PreprocessorStdoutTabProvider(@NotNull SettingsState state) {
        super(state, Tabs.PREPROCESSOR_STDOUT, "compilerexplorer.ShowPreprocessorStdoutTab", true, output -> output != null ? output.getStdout() : null);
    }
}
