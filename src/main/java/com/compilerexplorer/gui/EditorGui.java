package com.compilerexplorer.gui;

import com.compilerexplorer.common.*;
import com.compilerexplorer.datamodel.*;
import com.compilerexplorer.datamodel.state.SettingsState;
import com.compilerexplorer.gui.listeners.*;
import com.compilerexplorer.gui.tabs.*;
import com.compilerexplorer.gui.tracker.CaretTracker;
import com.google.common.collect.ImmutableList;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.ex.*;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.PlainTextFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.components.JBScrollPane;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

public class EditorGui extends RefreshableComponent<CompiledText> {
    public static final Key<EditorGui> KEY = Key.create(Constants.PROJECT_TITLE + ".EditorGui");

    @NotNull
    private static List<TabProvider> allTabs(@NotNull Project project) {
        return ImmutableList.of(
                new ProjectInfoTabProvider(project),
                new PreprocessorVersionStdoutTabProvider(project),
                new PreprocessorVersionStderrTabProvider(project),
                new PreprocessorVersionOutputTabProvider(project),
                new PreprocessorStdoutTabProvider(project),
                new PreprocessorStderrTabProvider(project),
                new PreprocessorOutputTabProvider(project),
                new SourceInfoTabProvider(project),
                new ExplorerSiteInfoTabProvider(project),
                new ExplorerSiteRawOutputTabProvider(project),
                new ExplorerRawInputTabProvider(project),
                new ExplorerRawOutputTabProvider(project),
                new ExplorerStdoutTabProvider(project),
                new ExplorerStderrTabProvider(project),
                new ExplorerOutputTabProvider(project),
                new ExplorerExecResultTabProvider(project)
        );
    }

    @NotNull
    private final JPanel mainPanel;
    @NotNull
    private final Project project;
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
    private final List<TabProvider> tabs;
    @Nullable
    private Tabs currentTab;
    @Nullable
    private Tabs requestedTab;
    @Nullable
    private Tabs selectedTab;

    public EditorGui(@NotNull Project project_, @NotNull SuppressionFlag suppressUpdates_, @NotNull Runnable notifyWhenEditorReady) {
        project = project_;
        suppressUpdates = suppressUpdates_;

        project.putUserData(EditorGui.KEY, this);

        mainPanel = new JPanel(new BorderLayout());
        editor = new EditorTextField(EditorFactory.getInstance().createDocument(""), project, PlainTextFileType.INSTANCE, true, false) {
            @Override
            @NotNull
            protected EditorEx createEditor() {
                EditorEx ed = super.createEditor();
                ed.setHorizontalScrollbarVisible(true);
                ed.setVerticalScrollbarVisible(true);
                ((EditorMarkupModel)ed.getMarkupModel()).setErrorStripeVisible(true);
                ed.setViewer(true);
                setupTabs(ed);
                notifyWhenEditorReady.run();
                return ed;
            }
        };
        editor.setFont(new Font("monospaced", editor.getFont().getStyle(), editor.getFont().getSize()));
        mainPanel.add(editor, BorderLayout.CENTER);

        caretTracker = new CaretTracker(locations -> withEditor(ed -> {
            withCurrentTabProvider(provider -> provider.highlightLocations(ed, locations));
            if (getState().getAutoscrollFromSource()) {
                scrollToClosestLocation(locations);
            }
        }));
        new AllEditorsListener(project, caretTracker::update);

        tabs = allTabs(project);

        project.getMessageBus().connect().subscribe(EditorColorsManager.TOPIC, new EditorColorsThemeChangeListener(this::applyThemeColors));
    }

    public void applyThemeColors() {
        withCurrentTabProvider(TabProvider::applyThemeColors);
        tabsCombobox.applyThemeColors();
    }

    @NotNull
    private static AnAction findAction(@NotNull String id) {
        return ActionManager.getInstance().getAction(id);
    }

    @NotNull
    public Component getComponent() {
        return mainPanel;
    }

    public void scrollFromSource() {
        scrollToClosestLocation(caretTracker.getLocations());
    }

    private void setupTabs(@NotNull EditorEx ed) {
        ((JBScrollPane) ed.getScrollPane()).setStatusComponent(tabsCombobox);
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
        withEditor(ed -> withCurrentTabProvider(provider -> provider.updateFolding(ed)));
    }

    @Override
    public void accept(@NotNull CompiledText compiledText_) {
        suppressUpdates.apply(() -> {
            super.accept(compiledText_);
            ApplicationManager.getApplication().assertIsDispatchThread();

            List<TabProvider> visibleTabs = tabs.stream()
                    .filter(factory -> getState().getShowAllTabs() || factory.isEnabled(compiledText_))
                    .toList();

            @Nullable Tabs newSelectedTab = null;

            if (selectedTab != null) {
                newSelectedTab = selectedTab;
                requestedTab = null;
            }
            if (newSelectedTab == null) {
                @Nullable TabProvider firstErrorTab = tabs.stream()
                        .filter(factory -> factory.isEnabled(compiledText_) && factory.isError(compiledText_))
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
                        .filter(factory -> factory.isEnabled(compiledText_))
                        .reduce((first, second) -> second)
                        .orElse(null);
                if (lastEnabledTab != null) {
                    newSelectedTab = lastEnabledTab.getTab();
                }
            }

            List<AnAction> tabActions = visibleTabs.stream()
                    .map(provider -> findAction(provider.actionId()))
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

    public void showTab(@NotNull Tabs tab) {
        selectedTab = tab;
        showTab(tab, false);
        selectedTab = null;
    }

    private void showTab(@NotNull Tabs tab, boolean forceRefresh) {
        tabsCombobox.selectAction(findTabAction(tab), true);
        if (currentTab != tab || forceRefresh) {
            if (currentTab != null && currentTab != tab) {
                int scrollPosition = withEditor(EditorGui::findCurrentScrollPosition, 0);
                getState().addToScrollPositions(currentTab, scrollPosition);
            }
            currentTab = tab;
            refresh();
            if (currentTab != null) {
                int scrollPosition = getState().getScrollPositions().getOrDefault(currentTab, 0);
                withEditor(ed -> scrollToPosition(ed, scrollPosition));
            }
        }
    }

    @Override
    public void refresh() {
        suppressRefresh.unlessApplied(() ->
            suppressRefresh.apply(() -> {
                suppressUpdates.unlessApplied(super::refresh);
                if (lastT != null) {
                    withCurrentTabProvider(provider -> {
                        FileType fileType = provider.getFileType(lastT);
                        Boolean[] provided = new Boolean[]{false};
                        provider.provide(lastT, text -> {
                            final int oldScrollPosition = withEditor(EditorGui::findCurrentScrollPosition, 0);

                            editor.setNewDocumentAndFileType(fileType, editor.getDocument());
                            editor.setText(fileType == PlainTextFileType.INSTANCE ? filterOutTerminalEscapeSequences(text) : text);
                            editor.setEnabled(true);

                            withEditor(ed -> scrollToPosition(ed, oldScrollPosition));
                            provided[0] = true;
                            return editor.getEditor(false);
                        });
                        if (provided[0]) {
                            withEditor(ed -> provider.highlightLocations(ed, caretTracker.getLocations()));
                        } else {
                            clearEditor();
                        }
                    });
                }
            })
        );
    }

    public void requestTab(@NotNull Tabs tab) {
        requestedTab = tab;
    }

    private void clearEditor() {
        editor.setNewDocumentAndFileType(PlainTextFileType.INSTANCE, editor.getDocument());
        editor.setText("");
        editor.setEnabled(false);
    }

    @NotNull
    private static String filterOutTerminalEscapeSequences(@NotNull String terminalText) {
        return terminalText.replaceAll("\u001B\\[[;\\d]*.", "");
    }

    @NotNull
    private SettingsState getState() {
        return CompilerExplorerSettingsProvider.getInstance(project).getState();
    }

    private void scrollToClosestLocation(@NotNull List<CompiledText.SourceLocation> locations) {
        ApplicationManager.getApplication().assertIsDispatchThread();
        withEditor(ed -> {
            Integer[] closestPosition = new Integer[]{-1};
            for (@NotNull CompiledText.SourceLocation location : locations) {
                withCurrentTabProvider(provider -> {
                    List<TabProvider.Range> ranges = provider.getRangesForLocation(location);
                    if (ranges != null) {
                        final int currentScrollPosition = findCurrentScrollPosition(ed);
                        int closestPositionDistance = -1;
                        for (TabProvider.Range range : ranges) {
                            int positionBegin = ed.offsetToXY(range.begin).y;
                            int diffBegin = Math.abs(positionBegin - currentScrollPosition);
                            if ((closestPositionDistance < 0) || (diffBegin < closestPositionDistance)) {
                                closestPositionDistance = diffBegin;
                                closestPosition[0] = positionBegin;
                            }
                            int positionEnd = ed.offsetToXY(range.end).y + ed.getLineHeight();
                            int diffEnd = Math.abs(positionEnd - currentScrollPosition);
                            if ((closestPositionDistance < 0) || (diffEnd < closestPositionDistance)) {
                                closestPositionDistance = diffEnd;
                                closestPosition[0] = positionEnd;
                            }
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
            if (getState().getEnableFolding()) {
                FoldingModelEx foldingModel = ed.getFoldingModel();
                foldingModel.runBatchFoldingOperation(() -> {
                    for (FoldRegion region : foldingModel.getAllFoldRegions()) {
                        region.setExpanded(isExpanded);
                    }
                });
                refresh();
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
        return findAction(findTabProvider(tab).actionId());
    }

    public boolean isTabEnabled(@NotNull Tabs tab) {
        return lastT != null && (getState().getShowAllTabs() || findTabProvider(tab).isEnabled(lastT));
    }
}
