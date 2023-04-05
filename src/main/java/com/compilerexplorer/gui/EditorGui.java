package com.compilerexplorer.gui;

import com.compilerexplorer.common.*;
import com.compilerexplorer.common.component.BaseRefreshableComponent;
import com.compilerexplorer.common.component.CEComponent;
import com.compilerexplorer.common.component.DataHolder;
import com.compilerexplorer.datamodel.*;
import com.compilerexplorer.datamodel.state.SettingsState;
import com.compilerexplorer.gui.listeners.*;
import com.compilerexplorer.gui.tabs.*;
import com.compilerexplorer.gui.tracker.CaretTracker;
import com.intellij.notification.NotificationType;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.editor.ex.*;
import com.intellij.openapi.fileChooser.FileChooserFactory;
import com.intellij.openapi.fileChooser.FileSaverDescriptor;
import com.intellij.openapi.fileChooser.FileSaverDialog;
import com.intellij.openapi.fileTypes.*;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileWrapper;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.SpinningProgressIcon;
import com.intellij.ui.components.JBScrollPane;
import com.twelvemonkeys.io.FileUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.io.PrintWriter;
import java.nio.file.Path;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class EditorGui extends BaseRefreshableComponent {
    @NonNls
    private static final Logger LOG = Logger.getInstance(EditorGui.class);
    public static final Key<EditorGui> KEY = Key.create("compilerexplorer.EditorGui");
    @NonNls
    @NotNull
    private static final String TERMINAL_ESCAPE_SEQUENCE_REGEXP = "\u001B\\[[;\\d]*.";

    @NotNull
    private final JPanel mainPanel;
    @NotNull
    private final Project project;
    @NotNull
    private final SettingsState state;
    @NotNull
    private final EditorTextField editor;
    @NotNull
    private final SuppressionFlag suppressUpdates;
    @NotNull
    private final SuppressionFlag suppressRefresh = new SuppressionFlag();
    @NotNull
    private final CaretTracker caretTracker;
    @NotNull
    private final TabsComboBox tabsCombobox = new TabsComboBox();
    @NotNull
    private final JLabel spinningLabel = new JLabel();
    @NotNull
    private final List<TabProvider> tabs;
    @Nullable
    private Tabs currentTab;
    @Nullable
    private Tabs requestedTab;
    @Nullable
    private Tabs selectedTab;
    @NotNull
    private final FoldingManager foldingManager;
    @Nullable
    List<TabProvider.FoldingRegion> lastFoldingRegions = null;

    public EditorGui(@NotNull CEComponent nextComponent, @NotNull Project project_, @NotNull SuppressionFlag suppressUpdates_, @NotNull Runnable editorReadyConsumer) {
        super(nextComponent);
        LOG.debug("created");

        project = project_;
        state = CompilerExplorerSettingsProvider.getInstance(project).getState();
        suppressUpdates = suppressUpdates_;
        foldingManager = new FoldingManager(project, state);

        project.putUserData(EditorGui.KEY, this);

        spinningLabel.setIcon(new SpinningProgressIcon());
        spinningLabel.setVisible(false);

        mainPanel = new JPanel(new BorderLayout());
        editor = new EditorTextField(EditorFactory.getInstance().createDocument(""), project, PlainTextFileType.INSTANCE, true, false);
        editor.addSettingsProvider(ed -> {
            LOG.debug("creating editor");
            suppressRefresh.unlessApplied(() -> editorCreated(ed, lastFoldingRegions));
            editorReadyConsumer.run();
        });

        editor.setFont(new Font("monospaced", editor.getFont().getStyle(), editor.getFont().getSize()));
        mainPanel.add(editor, BorderLayout.CENTER);

        caretTracker = new CaretTracker(locations -> withEditor(ed -> {
            withCurrentTabProvider(provider -> provider.highlightLocations(ed, locations));
            if (getState().getAutoscrollFromSource()) {
                scrollToClosestLocation(locations);
            }
        }));
        new AllEditorsListener(project, caretTracker::update);

        tabs = TabsFactory.create(project);

        requestTab(state.getLastOpenTab());
    }
    public void updateCaretTracker(@NotNull VirtualFile file, @Nullable Editor editor) {
        caretTracker.update(file, editor);
    }

    public void setSpinningIndicatorVisible(boolean visible) {
        spinningLabel.setVisible(visible);
    }

    public void applyThemeColors() {
        withCurrentTabProvider(TabProvider::applyThemeColors);
        tabsCombobox.applyThemeColors();
    }

    @NotNull
    public Component getComponent() {
        return mainPanel;
    }

    public void scrollFromSource() {
        scrollToClosestLocation(caretTracker.getLocations());
    }

    private void setupTabs(@NotNull EditorEx ed) {
        JPanel panel = new JPanel();
        panel.add(spinningLabel);
        panel.add(tabsCombobox);
        ((JBScrollPane) ed.getScrollPane()).setStatusComponent(panel);
    }

    @Nullable
    public EditorEx getEditor() {
        return editor.getEditor(false);
    }

    private void withEditor(@NotNull Consumer<EditorEx> consumer) {
        @Nullable EditorEx ed = getEditor();
        if (ed != null) {
            consumer.accept(ed);
        }
    }

    private <ReturnType> ReturnType withEditor(@NotNull Function<EditorEx, ReturnType> consumer, ReturnType defaultValue) {
        @Nullable EditorEx ed = getEditor();
        return ed != null ? consumer.apply(ed) : defaultValue;
    }

    public void updateGutter() {
        withEditor(ed -> withCurrentTabProvider(provider -> provider.updateGutter(ed)));
    }

    public void updateFolding() {
        withEditor(foldingManager::updateFolding);
    }

    @Override
    protected void doClear(@NotNull DataHolder data) {
        LOG.debug("doClear");
    }

    @Override
    protected void doRefresh(@NotNull DataHolder data) {
        LOG.debug("doRefresh");
        suppressUpdates.apply(() -> {
            ApplicationManager.getApplication().assertIsDispatchThread();

            List<TabProvider> visibleTabs = tabs.stream()
                    .filter(provider -> getState().getShowAllTabs() || provider.isEnabled(data))
                    .toList();

            @Nullable Tabs newSelectedTab = chooseNewTab(data, visibleTabs);

            List<AnAction> tabActions = visibleTabs.stream()
                    .map(provider -> ActionUtil.findAction(provider.actionId()))
                    .toList();
            @Nullable AnAction newSelectedTabAction = newSelectedTab != null ? findTabAction(newSelectedTab) : null;
            tabsCombobox.refreshModel(tabActions, newSelectedTabAction);
            if (newSelectedTab != null) {
                showTab(newSelectedTab, true);
            } else {
                currentTab = null;
                clearEditor();
            }
        });
    }

    private Tabs chooseNewTab(@NotNull DataHolder data, @NotNull List<TabProvider> visibleTabs) {
        @Nullable Tabs newSelectedTab = null;
        if (selectedTab != null) {
            newSelectedTab = selectedTab;
            requestedTab = null;
        }
        if (newSelectedTab == null) {
            @Nullable TabProvider firstErrorTab = tabs.stream()
                    .filter(provider -> provider.isEnabled(data) && provider.isError(data))
                    .findFirst()
                    .orElse(null);
            if (firstErrorTab != null) {
                newSelectedTab = firstErrorTab.getTab();
                if (requestedTab == null) {
                    requestedTab = currentTab;
                }
            }
        }
        if (newSelectedTab == null) {
            boolean requestedTabExists = visibleTabs.stream()
                    .anyMatch(provider -> provider.getTab() == requestedTab);
            if (requestedTabExists) {
                newSelectedTab = requestedTab;
                requestedTab = null;
            }
        }
        if (newSelectedTab == null) {
            boolean currentTabExists = visibleTabs.stream()
                    .anyMatch(provider -> provider.getTab() == currentTab);
            if (currentTabExists) {
                newSelectedTab = currentTab;
            }
        }
        if (newSelectedTab == null) {
            @Nullable TabProvider lastEnabledTab = tabs.stream()
                    .filter(provider -> provider.isEnabled(data))
                    .reduce((first, second) -> second)
                    .orElse(null);
            if (lastEnabledTab != null) {
                newSelectedTab = lastEnabledTab.getTab();
            }
        }
        return newSelectedTab;
    }

    public void showTab(@NotNull Tabs tab) {
        selectedTab = tab;
        showTab(tab, false);
        selectedTab = null;
    }

    private void showTab(@NotNull Tabs tab, boolean forceRefresh) {
        tabsCombobox.selectAction(findTabAction(tab), true);
        if (currentTab != tab || forceRefresh) {
            currentTab = tab;
            state.setLastOpenTab(tab);
            refresh(false);
            withEditor(this::restoreCurrentTabScrollPosition);
        }
    }

    public void saveCurrentTabScrollPosition() {
        DataHolder data = getLastData();
        if (data != null) {
            withCurrentTabProvider(provider -> {
                assert currentTab != null;

                int scrollPosition = withEditor(EditorGui::findCurrentScrollPosition, 0);
                if (provider.isError(data)) {
                    getState().addToScrollPositionsError(currentTab, scrollPosition);
                } else {
                    getState().addToScrollPositions(currentTab, scrollPosition);
                }
            });
        }
    }

    private void restoreCurrentTabScrollPosition(@NotNull EditorEx ed) {
        DataHolder data = getLastData();
        if (data != null) {
            withCurrentTabProvider(provider -> {
                assert currentTab != null;

                int scrollPosition;
                if (provider.isError(data)) {
                    scrollPosition = getState().getScrollPositionsError().getOrDefault(currentTab, 0);
                } else {
                    scrollPosition = getState().getScrollPositions().getOrDefault(currentTab, 0);
                }
                scrollToPosition(ed, scrollPosition);
            });
        }
    }

    @Override
    public void refresh(boolean reset) {
        LOG.debug("refresh " + reset);
        suppressRefresh.unlessApplied(() ->
            suppressRefresh.apply(() -> {
                suppressUpdates.unlessApplied(() -> super.refresh(reset));
                withCurrentTabProvider(provider -> {
                    DataHolder data = getLastData();
                    assert data != null;

                    FileType fileType = provider.getFileType(data);
                    Boolean[] provided = new Boolean[]{false};
                    provider.provide(data, (text, foldingRegions) -> {
                        editor.setNewDocumentAndFileType(fileType, editor.getDocument());
                        editor.setText(fileType == PlainTextFileType.INSTANCE ? filterOutTerminalEscapeSequences(text) : text);
                        editor.setEnabled(true);

                        if (foldingRegions.isEmpty()) {
                            foldingRegions = FoldingUtil.getFoldingForFileType(fileType, project, editor.getDocument());
                        }
                        lastFoldingRegions = foldingRegions.orElse(null);

                        withEditor(ed -> editorCreated(ed, lastFoldingRegions));
                        provided[0] = true;
                    });
                    if (!provided[0]) {
                        lastFoldingRegions = null;
                        clearEditor();
                    }
                });
            })
        );
    }

    private void editorCreated(@NotNull EditorEx ed, @Nullable List<TabProvider.FoldingRegion> foldingRegions) {
        ed.setHorizontalScrollbarVisible(true);
        ed.setVerticalScrollbarVisible(true);
        ((EditorMarkupModel)ed.getMarkupModel()).setErrorStripeVisible(true);
        ed.setViewer(true);
        setupTabs(ed);
        foldingManager.editorCreated(ed);
        foldingManager.set(getCurrentSourceFilename(getLastData()), currentTab, foldingRegions, ed);
        withCurrentTabProvider(provider -> {
            provider.editorCreated(ed);
            provider.highlightLocations(ed, caretTracker.getLocations());
        });
        restoreCurrentTabScrollPosition(ed);
        ed.getScrollingModel().addVisibleAreaListener(e -> saveCurrentTabScrollPosition());
    }

    public void requestTab(@NotNull Tabs tab) {
        requestedTab = tab;
    }

    private void clearEditor() {
        editor.setNewDocumentAndFileType(PlainTextFileType.INSTANCE, editor.getDocument());
        editor.setText("");
        editor.setEnabled(false);
        foldingManager.set(getCurrentSourceFilename(getLastData()), currentTab, null, getEditor());
    }

    @Nullable
    private String getCurrentSourceFilename(@Nullable DataHolder data) {
        return data != null ? data.get(SelectedSource.KEY).map(SelectedSource::getSelectedSource).map(s -> s.sourcePath).orElse(null) : null;
    }

    @NotNull
    private static String filterOutTerminalEscapeSequences(@NotNull String terminalText) {
        return terminalText.replaceAll(TERMINAL_ESCAPE_SEQUENCE_REGEXP, "");
    }

    @NotNull
    private SettingsState getState() {
        return state;
    }

    private void scrollToClosestLocation(@NotNull List<CompiledText.SourceLocation> locations) {
        ApplicationManager.getApplication().assertIsDispatchThread();
        withEditor(ed -> {
            Integer[] closestPosition = new Integer[]{-1};
            for (@NotNull CompiledText.SourceLocation location : locations) {
                withCurrentTabProvider(provider -> {
                    @NotNull List<TextRange> ranges = provider.getRangesForLocation(location);
                    final int currentScrollPosition = findCurrentScrollPosition(ed);
                    int closestPositionDistance = -1;
                    for (TextRange range : ranges) {
                        int positionBegin = ed.offsetToXY(range.getStartOffset()).y;
                        int diffBegin = Math.abs(positionBegin - currentScrollPosition);
                        if ((closestPositionDistance < 0) || (diffBegin < closestPositionDistance)) {
                            closestPositionDistance = diffBegin;
                            closestPosition[0] = positionBegin;
                        }
                        int positionEnd = ed.offsetToXY(range.getEndOffset()).y + ed.getLineHeight();
                        int diffEnd = Math.abs(positionEnd - currentScrollPosition);
                        if ((closestPositionDistance < 0) || (diffEnd < closestPositionDistance)) {
                            closestPositionDistance = diffEnd;
                            closestPosition[0] = positionEnd;
                        }
                    }
                });
            }
            if (closestPosition[0] >= 0) {
                scrollToPosition(ed, closestPosition[0] - (ed.getScrollingModel().getVisibleAreaOnScrollingFinished().height / 2));
            }
        });
    }

    public void expandAllFolding(boolean isExpanded) {
        withEditor(ed -> {
            foldingManager.expandAllFolding(ed, isExpanded);
            if (getState().getEnableFolding()) {
                refresh(false);
            }
        });
    }

    private static int findCurrentScrollPosition(@NotNull Editor ed) {
        return ed.getScrollingModel().getVisibleAreaOnScrollingFinished().y;
    }

    private static void scrollToPosition(@NotNull Editor ed, int y) {
        boolean useAnimation = !ed.getScrollingModel().getVisibleAreaOnScrollingFinished().equals(ed.getScrollingModel().getVisibleArea());
        if (!useAnimation) ed.getScrollingModel().disableAnimation();
        ed.getScrollingModel().scrollVertically(y);
        if (!useAnimation) ed.getScrollingModel().enableAnimation();
    }

    @NotNull
    private TabProvider findTabProvider(@NotNull Tabs tab) {
        return tabs.stream()
                .filter(provider -> provider.getTab() == tab)
                .findFirst()
                .orElse(tabs.get(tabs.size() - 1));
    }

    private void withCurrentTabProvider(Consumer<TabProvider> consumer) {
        if (currentTab != null) {
            consumer.accept(findTabProvider(currentTab));
        }
    }

    @NotNull
    private AnAction findTabAction(@NotNull Tabs tab) {
        return ActionUtil.findAction(findTabProvider(tab).actionId());
    }

    public boolean isTabEnabled(@NotNull Tabs tab) {
        return getLastData() != null && (getState().getShowAllTabs() || findTabProvider(tab).isEnabled(getLastData()));
    }

    public void saveCurrentTabAs() {
        @Nullable final DataHolder data = getLastData();
        @Nullable final EditorEx ed = getEditor();
        if (currentTab != null && data != null && ed != null) {
            @Nullable Path directory = null;
            @NotNull final TabProvider currentTabProvider = findTabProvider(currentTab);
            Presentation presentation = ActionUtil.createPresentation(ActionUtil.findAction(currentTabProvider.actionId()), getComponent());
            @Nullable String filenameWithPrefix = presentation.getText();

            @Nullable final String sourceFilename = getCurrentSourceFilename(data);
            if (sourceFilename != null) {
                @NotNull final Path path = Path.of(sourceFilename);
                directory = path.getParent();
                if (currentTabProvider.isSourceSpecific()) {
                    @NotNull String sourceBasename = FileUtil.getBasename(path.getFileName().toString());
                    if (filenameWithPrefix != null) {
                        filenameWithPrefix = Bundle.format("compilerexplorer.EditorGui.SaveTabFilename", "TabName", filenameWithPrefix, "SourceBasename", sourceBasename);
                    } else {
                        filenameWithPrefix = sourceBasename;
                    }
                }
            }

            @NotNull final Presentation saveAsPresentation = ActionUtil.createPresentation(ActionUtil.findAction("compilerexplorer.SaveCurrentTabAs"), getComponent());
            @NotNull final FileSaverDescriptor descriptor = new FileSaverDescriptor(saveAsPresentation.getText(), saveAsPresentation.getDescription(), currentTabProvider.defaultExtension(data), null);
            @NotNull final FileSaverDialog dialog = FileChooserFactory.getInstance().createSaveFileDialog(descriptor, project);
            @Nullable final VirtualFileWrapper file = dialog.save(directory, filenameWithPrefix);
            if (file != null) {
                @NotNull final String filename = file.getFile().getPath();
                try (PrintWriter out = new PrintWriter(filename)) {
                    out.println(ed.getDocument().getText());
                } catch (Exception exception) {
                    String errorMessage = Bundle.format("compilerexplorer.EditorGui.SaveTabError", "Filename", filename, "Exception", exception.getMessage());
                    LOG.error(errorMessage);
                    Constants.NOTIFICATION_GROUP
                            .createNotification(errorMessage, NotificationType.ERROR)
                            .notify(project);
                }
            }
        }
    }
}
