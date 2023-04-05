package com.compilerexplorer.gui.json;

import com.intellij.json.JsonElementTypes;
import com.intellij.json.JsonTokenType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.TokenType;
import com.intellij.psi.impl.source.tree.LeafPsiElement;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.tree.IFileElementType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.*;
import java.util.function.BiFunction;

public class JsonFoldingUtil {
    @NonNls
    @NotNull
    private static final String ROOT_LABEL = "root";
    private static final BiFunction<String, String, String> CHILD_NAME = (parent, child) -> parent + "." + child;
    private static final BiFunction<String, Integer, String> ARRAY_ELEMENT_NAME = (parent, i) -> parent + "[" + i + "]";

    @NotNull
    public static String getJsonLabel(@NotNull ASTNode node, @NotNull Map<ASTNode, String> cache) {
        String label = "";
        final @NotNull ASTNode parent = node.getTreeParent();
        final @NotNull IElementType parentType = parent.getElementType();
        if (parentType instanceof IFileElementType) {
            label = ROOT_LABEL;
        } else if (parentType == JsonElementTypes.PROPERTY) {
            final @NotNull ASTNode parentObject = parent.getTreeParent();
            final @NotNull ASTNode nameNode = parent.getFirstChildNode().getFirstChildNode();
            @NotNull String name = nameNode.getPsi(LeafPsiElement.class).getText();
            if (nameNode.getElementType() == JsonElementTypes.DOUBLE_QUOTED_STRING || nameNode.getElementType() == JsonElementTypes.SINGLE_QUOTED_STRING) {
                name = name.substring(1, name.length() - 1);
            }
            label = CHILD_NAME.apply(cache.get(parentObject), name);
        } else if (parentType == JsonElementTypes.ARRAY) {
            label = cache.get(node);
            if (label == null) {
                int i = 0;
                ASTNode child = parent.getFirstChildNode();
                while (child != null) {
                    if (!(child.getElementType() instanceof JsonTokenType) && (child.getElementType() != TokenType.WHITE_SPACE)) {
                        @NotNull final String childLabel = ARRAY_ELEMENT_NAME.apply(cache.get(parent), i);
                        cache.put(child, childLabel);
                        if (child == node) {
                            label = childLabel;
                        }
                        ++i;
                    }
                    child = child.getTreeNext();
                }
            }
        }
        assert label != null;
        cache.put(node, label);
        return label;
    }
}
