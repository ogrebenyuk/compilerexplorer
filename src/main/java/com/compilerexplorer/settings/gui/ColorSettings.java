package com.compilerexplorer.settings.gui;

import com.compilerexplorer.common.Constants;
import com.google.common.collect.ImmutableMap;
import com.intellij.openapi.editor.colors.TextAttributesKey;
import com.intellij.openapi.fileTypes.SyntaxHighlighter;
import com.intellij.openapi.options.colors.AttributesDescriptor;
import com.intellij.openapi.options.colors.ColorDescriptor;
import com.intellij.openapi.options.colors.ColorSettingsPage;
import com.intellij.psi.codeStyle.DisplayPriority;
import com.intellij.psi.codeStyle.DisplayPrioritySortable;
import com.jetbrains.cidr.lang.asm.AsmSyntaxHighlighter;
import icons.CompilerExplorerIcons;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.Map;

import static com.compilerexplorer.common.Constants.HIGHLIGHT_COLOR;

public class ColorSettings implements ColorSettingsPage, DisplayPrioritySortable {
    @NotNull
    private static final AttributesDescriptor @NotNull [] DESCRIPTORS = new AttributesDescriptor[] {
            new AttributesDescriptor(() -> "Highlight", HIGHLIGHT_COLOR)
    };

    @NotNull
    private static final Map<String, TextAttributesKey> TAG_TO_DESCRIPTOR_MAP =
            ImmutableMap.<String, TextAttributesKey>builder()
                    .put("highlight", HIGHLIGHT_COLOR)
                    .build();

    @Override
    @NotNull
    public AttributesDescriptor @NotNull [] getAttributeDescriptors() {
        return DESCRIPTORS;
    }

    @Override
    @NotNull
    public ColorDescriptor @NotNull [] getColorDescriptors() {
        return ColorDescriptor.EMPTY_ARRAY;
    }

    @Override
    @NotNull
    public String getDisplayName() {
        return Constants.COLOR_SETTINGS_TITLE;
    }

    @Override
    @Nullable
    public Icon getIcon() {
        return CompilerExplorerIcons.ToolWindow;
    }

    @Override
    @NotNull
    public SyntaxHighlighter getHighlighter() {
        return new AsmSyntaxHighlighter();
    }

    @Override
    @NotNull
    public String getDemoText() {
        return """
                main:
                    pushq   %rbp  #
                    movq    %rsp, %rbp      #,
                    leaq    .LC0(%rip), %rax        #, tmp85
                    movq    %rax, %rsi      # tmp85,
                <highlight>    movq    std::cout@GOTPCREL(%rip), %rax  #, tmp87
                    movq    %rax, %rdi      # tmp86,
                    call    std::basic_ostream<...>& std::operator<< <...>(std::basic_ostream<...>&, char const*)@PLT     #
                    movq    std::basic_ostream<...>& std::endl<...>(std::basic_ostream<...>&)@GOTPCREL(%rip), %rdx #, tmp89
                    movq    %rdx, %rsi      # tmp88,
                </highlight>    movq    %rax, %rdi      # _1,
                    call    std::basic_ostream<...>::operator<<(std::basic_ostream<...>& (*)(std::basic_ostream<...>&))@PLT    #
                    movl    $0, %eax        #, _6
                    popq    %rbp    #
                    ret
                """;
    }

    @Override
    @Nullable
    public Map<String, TextAttributesKey> getAdditionalHighlightingTagToDescriptorMap() {
        return TAG_TO_DESCRIPTOR_MAP;
    }

    @Override
    @NotNull
    public DisplayPriority getPriority() {
        return DisplayPriority.COMMON_SETTINGS;
    }
}
