package com.compilerexplorer.gui.tabs;

import com.compilerexplorer.common.Tabs;
import com.compilerexplorer.common.component.DataHolder;
import com.compilerexplorer.datamodel.ProjectSources;
import com.compilerexplorer.datamodel.state.SettingsState;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public interface NoSources extends Base {
    @NotNull
    default DataHolder dataWithNoSources() {
        return new DataHolder();
    }

    @NotNull
    default DataHolder dataWithEmptySources() {
        return dataWithNoSources().with(ProjectSources.KEY, new ProjectSources(new ArrayList<>()));
    }

    @NotNull
    default SettingsState stateWithNoSources() {
        return new SettingsState();
    }

    @NotNull
    default List<CollectedTabContent> resultWithNoSources() {
        List<CollectedTabContent> result = new ArrayList<>();
        result.add(error(Tabs.PROJECT_INFO,                  ENABLED, "compilerexplorer.ProjectInfoTabProvider.NoSources"));
        result.add(message(Tabs.PREPROCESSOR_VERSION_STDOUT, DISABLED, "compilerexplorer.BasePreprocessorVersionTabProvider.WasNotRun"));
        result.add(message(Tabs.PREPROCESSOR_VERSION_STDERR, DISABLED, "compilerexplorer.BasePreprocessorVersionTabProvider.WasNotRun"));
        result.add(message(Tabs.PREPROCESSOR_VERSION_OUTPUT, DISABLED, "compilerexplorer.PreprocessorVersionOutputTabProvider.WasNotRun"));
        result.add(message(Tabs.PREPROCESSOR_STDOUT,         DISABLED, "compilerexplorer.BasePreprocessorTabProvider.WasNotRun"));
        result.add(message(Tabs.PREPROCESSOR_STDERR,         DISABLED, "compilerexplorer.BasePreprocessorTabProvider.WasNotRun"));
        result.add(message(Tabs.PREPROCESSOR_OUTPUT,         DISABLED, "compilerexplorer.BasePreprocessorTabProvider.WasNotRun"));
        result.add(message(Tabs.SOURCE_INFO,                 DISABLED, "compilerexplorer.SourceInfoTabProvider.NoSelection"));
        result.add(message(Tabs.EXPLORER_SITE_INFO,          DISABLED, "compilerexplorer.BaseExplorerSiteUtilProvider.NotQueried"));
        result.add(message(Tabs.EXPLORER_SITE_RAW_OUTPUT,    DISABLED, "compilerexplorer.BaseExplorerSiteUtilProvider.NotQueried"));
        result.add(message(Tabs.EXPLORER_RAW_INPUT,          DISABLED, "compilerexplorer.ExplorerRawInputTabProvider.WasNotRun"));
        result.add(message(Tabs.EXPLORER_RAW_OUTPUT,         DISABLED, "compilerexplorer.ExplorerRawOutputTabProvider.WasNotRun"));
        result.add(message(Tabs.EXPLORER_STDOUT,             DISABLED, "compilerexplorer.ExplorerStdoutTabProvider.WasNotRun"));
        result.add(message(Tabs.EXPLORER_STDERR,             DISABLED, "compilerexplorer.ExplorerStderrTabProvider.WasNotRun"));
        result.add(message(Tabs.EXPLORER_OUTPUT,             DISABLED, "compilerexplorer.ExplorerOutputTabProvider.WasNotRun"));
        result.add(message(Tabs.EXPLORER_OUTPUT_DEVICE_1,    DISABLED, "compilerexplorer.ExplorerOutputTabProvider.WasNotRun"));
        result.add(message(Tabs.EXPLORER_OUTPUT_DEVICE_2,    DISABLED, "compilerexplorer.ExplorerOutputTabProvider.WasNotRun"));
        result.add(message(Tabs.EXPLORER_OUTPUT_DEVICE_3,    DISABLED, "compilerexplorer.ExplorerOutputTabProvider.WasNotRun"));
        result.add(message(Tabs.EXPLORER_OUTPUT_DEVICE_4,    DISABLED, "compilerexplorer.ExplorerOutputTabProvider.WasNotRun"));
        result.add(message(Tabs.EXPLORER_OUTPUT_DEVICE_5,    DISABLED, "compilerexplorer.ExplorerOutputTabProvider.WasNotRun"));
        result.add(message(Tabs.EXPLORER_EXEC_RESULT,        DISABLED, "compilerexplorer.ExplorerExecResultTabProvider.WasNotRun"));
        result.add(content(Tabs.EVERYTHING,                  DISABLED, JSON));
        return result;
    }

    default void set(@NotNull List<CollectedTabContent> result, @NotNull CollectedTabContent content) {
        for (int i = 0; i < result.size(); ++i) {
            if (result.get(i).tab == content.tab) {
                result.set(i, content);
            }
        }
    }
}
