package com.compilerexplorer.gui.tabs.exploreroutput;

import com.compilerexplorer.common.Bundle;
import com.compilerexplorer.common.TooltipUtil;
import com.intellij.openapi.editor.Editor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class LineNumberAnnotationProvider extends BaseTextAnnotationGutterProvider {
    @Override
    @NotNull
    public String getLineText(int line, @Nullable Editor ed) {
        return String.valueOf(oneBasedLine(line));
    }
    @Override
    @Nullable
    public String getToolTip(int line, @Nullable Editor ed) {
        return getTooltipText(oneBasedLine(line));
    }

    private static int oneBasedLine(int zeroBasedLine) {
        return zeroBasedLine + 1;
    }

    @NotNull
    private String getTooltipText(@NotNull Integer displayLine) {
        return TooltipUtil.prettify(Bundle.format("compilerexplorer.LineNumberAnnotationProvider.Tooltip", "Line", Integer.toString(displayLine)));
    }
}
