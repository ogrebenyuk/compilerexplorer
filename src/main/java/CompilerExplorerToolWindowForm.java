import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public class CompilerExplorerToolWindowForm {
    private JButton refreshButton;
    private JPanel content;

    @NotNull
    JPanel getContent() {
        return content;
    }

    @NotNull
    JButton getRefreshButton() {
        return refreshButton;
    }
}
