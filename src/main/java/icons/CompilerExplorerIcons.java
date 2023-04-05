package icons;

import com.intellij.openapi.util.IconLoader;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class CompilerExplorerIcons {
    @NonNls
    @NotNull
    private static final String TOOL_WINDOW_ICON_PATH = "/icons/toolWindow.svg";

    public static final Icon ToolWindow = IconLoader.getIcon(TOOL_WINDOW_ICON_PATH, CompilerExplorerIcons.class);
}
