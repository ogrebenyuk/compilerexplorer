import org.jetbrains.annotations.NotNull;

public class CompilerExplorerEvents {
    private CompilerExplorer explorer;
    private CompilerExplorerToolWindowForm form;

    void set(CompilerExplorer explorer_, CompilerExplorerToolWindowForm form_) {
        explorer = explorer_;
        form = form_;
    }

    void refresh() {
        explorer.refresh();
    }

    void setText(@NotNull String text) {
        form.setText(text);
    }
}
