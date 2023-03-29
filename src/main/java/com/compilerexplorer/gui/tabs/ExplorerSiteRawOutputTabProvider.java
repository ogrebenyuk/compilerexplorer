package com.compilerexplorer.gui.tabs;

import com.compilerexplorer.common.Tabs;
import com.compilerexplorer.datamodel.CompiledText;
import com.intellij.json.JsonFileType;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class ExplorerSiteRawOutputTabProvider extends TabProvider {
    public ExplorerSiteRawOutputTabProvider(@NotNull Project project) {
        super(project, Tabs.EXPLORER_SITE_RAW_OUTPUT, "compilerexplorer.ShowExplorerSiteRawOutputTab", JsonFileType.INSTANCE);
    }

    @Override
    public boolean isEnabled(@NotNull CompiledText compiledText) {
        return false;
    }

    @Override
    public boolean isError(@NotNull CompiledText compiledText) {
        return false;
    }

    @Override
    public void provide(@NotNull CompiledText compiledText, @NotNull Function<String, EditorEx> textConsumer) {
        if (compiledText.sourceRemoteMatched.preprocessedSource.sourceCompilerSettings.sourceSettingsConnected.remoteCompilersRawOutput != null) {
            textConsumer.apply(compiledText.sourceRemoteMatched.preprocessedSource.sourceCompilerSettings.sourceSettingsConnected.remoteCompilersRawOutput);
        }
    }
}
