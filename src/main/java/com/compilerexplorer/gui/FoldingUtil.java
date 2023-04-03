package com.compilerexplorer.gui;

import com.compilerexplorer.gui.json.JsonFoldingUtil;
import com.compilerexplorer.gui.tabs.TabProvider;
import com.intellij.json.JsonFileType;
import com.intellij.lang.ASTNode;
import com.intellij.lang.Language;
import com.intellij.lang.folding.FoldingBuilder;
import com.intellij.lang.folding.FoldingDescriptor;
import com.intellij.lang.folding.LanguageFolding;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiFileFactory;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiFunction;

public class FoldingUtil {
    @NotNull
    public static Optional<List<TabProvider.FoldingRegion>> getFoldingForFileType(@NotNull FileType fileType,
                                                                                  @NotNull Project project,
                                                                                  @NotNull Document document) {
        if (fileType instanceof JsonFileType jsonFileType) {
            return getFolding(jsonFileType.getLanguage(), project, document, JsonFoldingUtil::getJsonLabel);
        }
        return Optional.empty();
    }

    @NotNull
    private static Optional<List<TabProvider.FoldingRegion>> getFolding(@NotNull Language language,
                                                                        @NotNull Project project,
                                                                        @NotNull Document document,
                                                                        @NotNull BiFunction<ASTNode, Map<ASTNode, String>, String> labelProvider) {
        FoldingBuilder builder = LanguageFolding.INSTANCE.forLanguage(language);
        PsiFile psiFile = PsiFileFactory.getInstance(project).createFileFromText("foo.bar", language, document.getText(), true, true, true);
        FoldingDescriptor @NotNull [] descriptors = builder.buildFoldRegions(psiFile.getNode(), document);
        @NotNull Map<ASTNode, String> cache = new HashMap<>();
        return Optional.of(Arrays.stream(descriptors)
                .map(descriptor -> new TabProvider.FoldingRegion(descriptor.getRange(), labelProvider.apply(descriptor.getElement(), cache), descriptor.getPlaceholderText() != null ? descriptor.getPlaceholderText() : ""))
                .toList());
    }
}
