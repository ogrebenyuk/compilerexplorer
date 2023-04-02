package com.compilerexplorer.gui.tabs;

import com.compilerexplorer.common.Tabs;
import com.compilerexplorer.datamodel.CompilerResult;
import com.compilerexplorer.datamodel.SelectedSourceCompiler;
import com.compilerexplorer.datamodel.json.JsonSerializationVisitor;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;

public class PreprocessorVersionOutputTabProvider extends BasePreprocessorVersionTabProvider {
    public PreprocessorVersionOutputTabProvider(@NotNull Project project) {
        super(project, Tabs.PREPROCESSOR_VERSION_OUTPUT, "compilerexplorer.ShowPreprocessorVersionOutputTab", true, true, PreprocessorVersionOutputTabProvider::getText);
    }

    @NotNull
    private static String getText(@NotNull SelectedSourceCompiler selectedSourceCompiler, @NotNull CompilerResult.Output output) {
        if (producedNoResult(selectedSourceCompiler)) {
            return getPreprocessorErrorMessage(output);
        } else {
            Gson gson = JsonSerializationVisitor.createDebugSerializer().getGson();
            JsonObject object = new JsonObject();
            object.addProperty("cached", selectedSourceCompiler.getCached());
            object.addProperty("canceled", selectedSourceCompiler.getCanceled());
            object.addProperty("isSupportedCompilerType", selectedSourceCompiler.getIsSupportedCompilerType());
            object.add("commandLine", gson.toJsonTree(selectedSourceCompiler.getResult().map(CompilerResult::getCommandLine).orElse(null)));
            object.add("workingDirectory", gson.toJsonTree(selectedSourceCompiler.getResult().map(CompilerResult::getWorkingDir).orElse(null)));
            object.add("result", gson.toJsonTree(selectedSourceCompiler.getLocalCompilerSettings().orElse(null)));
            return gson.toJson(object);
        }
    }
}
