package com.compilerexplorer.gui.tabs;

import com.compilerexplorer.common.Tabs;
import com.compilerexplorer.datamodel.CompiledText;
import com.compilerexplorer.datamodel.json.JsonSerializationVisitor;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.intellij.json.JsonFileType;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

import java.util.function.Function;

public class PreprocessorVersionOutputTabProvider extends PreprocessorTabProvider {
    public PreprocessorVersionOutputTabProvider(@NotNull Project project) {
        super(project, Tabs.PREPROCESSOR_VERSION_OUTPUT, "compilerexplorer.ShowPreprocessorVersionOutputTab", JsonFileType.INSTANCE);
    }

    @Override
    public boolean isEnabled(@NotNull CompiledText compiledText) {
        return compiledText.sourceRemoteMatched.preprocessedSource.sourceCompilerSettings.sourceSettingsConnected.sourceSettings.isValid()
                && compiledText.sourceRemoteMatched.preprocessedSource.sourceCompilerSettings.localCompilerSettings == null;
    }

    @Override
    public boolean isError(@NotNull CompiledText compiledText) {
        return compiledText.sourceRemoteMatched.preprocessedSource.sourceCompilerSettings.sourceSettingsConnected.sourceSettings.isValid()
                && compiledText.sourceRemoteMatched.preprocessedSource.sourceCompilerSettings.localCompilerSettings == null;
    }

    @Override
    public void provide(@NotNull CompiledText compiledText, @NotNull Function<String, EditorEx> textConsumer) {
        if (compiledText.sourceRemoteMatched.preprocessedSource.sourceCompilerSettings.sourceSettingsConnected.sourceSettings.isValid()) {
            if (compiledText.sourceRemoteMatched.preprocessedSource.sourceCompilerSettings.localCompilerSettings != null) {
                Gson gson = JsonSerializationVisitor.createDebugSerializer().getGson();
                JsonObject object = new JsonObject();
                object.addProperty("cached", compiledText.sourceRemoteMatched.preprocessedSource.sourceCompilerSettings.isCached());
                if (!compiledText.sourceRemoteMatched.preprocessedSource.sourceCompilerSettings.isCached()) {
                    object.addProperty("isSupportedCompilerType", compiledText.sourceRemoteMatched.preprocessedSource.sourceCompilerSettings.isSupportedCompilerType);
                    object.add("commandLine", gson.toJsonTree(compiledText.sourceRemoteMatched.preprocessedSource.sourceCompilerSettings.versionerCommandLine));
                    object.add("workingDirectory", gson.toJsonTree(compiledText.sourceRemoteMatched.preprocessedSource.sourceCompilerSettings.versionerWorkingDir));
                }
                object.add("result", gson.toJsonTree(compiledText.sourceRemoteMatched.preprocessedSource.sourceCompilerSettings.localCompilerSettings));
                String text = gson.toJson(object);
                textConsumer.apply(text);
            } else {
                showPreprocessorError(compiledText.sourceRemoteMatched.preprocessedSource.sourceCompilerSettings.versionerException,
                        compiledText.sourceRemoteMatched.preprocessedSource.sourceCompilerSettings.versionerExitCode,
                        compiledText.sourceRemoteMatched.preprocessedSource.sourceCompilerSettings.versionerStderr,
                        textConsumer);
            }
        }
    }
}
