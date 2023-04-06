package com.compilerexplorer.gui.tabs;

import com.compilerexplorer.common.Tabs;
import com.compilerexplorer.common.component.DataHolder;
import com.compilerexplorer.datamodel.ProjectSources;
import com.compilerexplorer.datamodel.SelectedSource;
import com.compilerexplorer.datamodel.state.SettingsState;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface WithSelectedSource extends WithSources {
    enum Status {WITHOUT_SELECTION, WITH_SELECTION}

    @NotNull
    default DataHolder dataWithSelectedSource(@NotNull Status status) {
        DataHolder data = dataWithSources();
        if (status == Status.WITH_SELECTION) {
            data.get(ProjectSources.KEY).ifPresent(sources ->
                data.put(SelectedSource.KEY, new SelectedSource(sources.getSources().get(0)))
            );
        }
        return data;
    }

    @NotNull
    default SettingsState stateWithSelectedSource(@NotNull Status status) {
        return stateWithSources();
    }

    @NotNull
    default List<CollectedTabContent> resultWithSelectedSource(@NotNull Status status) {
        @NotNull List<CollectedTabContent> result = resultWithSources();
        if (status == Status.WITH_SELECTION) {
            set(result, content(Tabs.SOURCE_INFO, DISABLED, JSON));
        }
        return result;
    }
}
