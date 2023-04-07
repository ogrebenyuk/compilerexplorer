package com.compilerexplorer.gui.tabs;

import com.compilerexplorer.common.Tabs;
import com.compilerexplorer.common.component.DataHolder;
import com.compilerexplorer.datamodel.ProjectSources;
import com.compilerexplorer.datamodel.SourceSettings;
import com.compilerexplorer.datamodel.state.SettingsState;
import org.jetbrains.annotations.NotNull;
import com.jetbrains.cidr.system.LocalHost;

import java.util.List;

public interface WithSources extends NoSources {
    @NotNull
    default DataHolder dataWithSources() {
        DataHolder data = dataWithEmptySources();
        data.get(ProjectSources.KEY).ifPresent(sources ->
            sources.getSources().add(new SourceSettings("sourcePath",
                "sourceName",
                "myLanguage",
                "languageSwitch",
                "compilerPath",
                "compilerWorkingDir",
                "compilerKind",
                List.of("mySwitches"),
                new LocalHost())
            )
        );
        return data;
    }

    @NotNull
    default SettingsState stateWithSources() {
        return stateWithNoSources();
    }

    @NotNull
    default List<CollectedTabContent> resultWithSources() {
        @NotNull List<CollectedTabContent> result = resultWithNoSources();
        set(result, content(Tabs.PROJECT_INFO, DISABLED, JSON));
        set(result, error(Tabs.SOURCE_INFO, ENABLED, "compilerexplorer.SourceInfoTabProvider.NoSelection"));
        return result;
    }
}
