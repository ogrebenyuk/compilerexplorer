package com.compilerexplorer.gui;

import com.compilerexplorer.common.DisposableParentProjectService;
import com.compilerexplorer.common.SuppressionFlag;
import com.compilerexplorer.common.Tabs;
import com.compilerexplorer.datamodel.state.SettingsState;
import com.compilerexplorer.gui.tabs.TabProvider;
import com.intellij.openapi.actionSystem.ActionManager;
import com.intellij.openapi.actionSystem.DefaultActionGroup;
import com.intellij.openapi.editor.FoldRegion;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.editor.ex.EditorGutterComponentEx;
import com.intellij.openapi.editor.ex.FoldingListener;
import com.intellij.openapi.editor.ex.FoldingModelEx;
import com.intellij.openapi.project.Project;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public class FoldingManager {
    @NotNull
    private static final String POPUP_ACTIONS_GROUP_ID = "compilerexplorer.EditorPopupGroup";
    @NotNull
    private static final String FOLDING_POPUP_ACTIONS_GROUP_ID = "compilerexplorer.FoldingEditorPopupGroup";
    @NotNull
    private static final DefaultActionGroup POPUP_ACTIONS = new DefaultActionGroup(ActionManager.getInstance().getAction(POPUP_ACTIONS_GROUP_ID));
    @NotNull
    private static final DefaultActionGroup FOLDING_POPUP_ACTIONS = new DefaultActionGroup(ActionManager.getInstance().getAction(FOLDING_POPUP_ACTIONS_GROUP_ID));

    @NotNull
    private final Project project;
    @NotNull
    private final SettingsState state;
    @NotNull
    private final SuppressionFlag suppressFoldingUpdates = new SuppressionFlag();
    @Nullable
    private String currentFilename;
    @Nullable
    private Tabs currentTab;
    private boolean foldingEnablesForThisFile = false;
    @NotNull
    private final Map<FoldRegion, String> regionToLabelMap = new HashMap<>();
    @NotNull
    private final FoldingListener foldingChangeListener = new FoldingListener() {
        @Override
        public void onFoldRegionStateChange(@NotNull FoldRegion region) {
            FoldingListener.super.onFoldRegionStateChange(region);
            updateExpanded(region);
        }
    };

    public FoldingManager(@NotNull Project project_, @NotNull SettingsState state_) {
        project = project_;
        state = state_;
    }

    public void editorCreated(@NotNull EditorEx ed) {
        updateFolding(ed);
        ed.getFoldingModel().addListener(foldingChangeListener, DisposableParentProjectService.getInstance(project));
    }

    public void set(@Nullable String filename, @Nullable Tabs tab, @Nullable List<TabProvider.FoldingRegion> foldingRegions, @Nullable EditorEx ed) {
        currentFilename = filename;
        currentTab = tab;
        foldingEnablesForThisFile = foldingRegions != null;
        Set<String> foldedLabels = filename != null && tab != null ? state.findFoldedLabels(filename, tab) : null;
        removeObsoleteFoldedLabels(foldingRegions, foldedLabels);
        regionToLabelMap.clear();
        if (ed != null) {
            setupGutter(ed);
            updateFolding(ed, true);
            FoldingModelEx foldingModel = ed.getFoldingModel();
            foldingModel.runBatchFoldingOperation(() -> withFoldingUpdatesSuppressed(foldingModel::clearFoldRegions));
            if (foldingRegions != null) {
                foldingModel.runBatchFoldingOperation(() -> withFoldingUpdatesSuppressed(() -> {
                    for (TabProvider.FoldingRegion foldingRegion : foldingRegions) {
                        @Nullable FoldRegion region = foldingModel.addFoldRegion(foldingRegion.range.getStartOffset(), foldingRegion.range.getEndOffset(), foldingRegion.placeholderText);
                        if (region != null) {
                            region.setExpanded(foldedLabels == null || foldingRegion.label.isEmpty() || !foldedLabels.contains(foldingRegion.label));
                            regionToLabelMap.put(region, foldingRegion.label);
                        }
                    }
                }));
            }
            updateFolding(ed);
        }
    }

    private void updateExpanded(@NotNull FoldRegion region) {
        unlessFoldingUpdatesSuppressed(() -> {
            if (currentFilename != null && currentTab != null) {
                @Nullable String label = regionToLabelMap.get(region);
                if (label != null && !label.isEmpty()) {
                    if (region.isExpanded()) {
                        state.removeFoldedLabel(currentFilename, currentTab, label);
                    } else {
                        state.addFoldedLabel(currentFilename, currentTab, label);
                    }
                }
            }
        });
    }

    public void updateFolding(@NotNull EditorEx ed) {
        updateFolding(ed, isFoldingEnabled());
    }

    private void updateFolding(@NotNull EditorEx ed, boolean enable) {
        ed.getFoldingModel().setFoldingEnabled(enable);
        ed.getSettings().setFoldingOutlineShown(enable);
    }

    public void expandAllFolding(@NotNull EditorEx ed, boolean isExpanded) {
        if (isFoldingEnabled()) {
            FoldingModelEx foldingModel = ed.getFoldingModel();
            foldingModel.runBatchFoldingOperation(() -> {
                for (FoldRegion region : foldingModel.getAllFoldRegions()) {
                    region.setExpanded(isExpanded);
                }
            });
        }
    }

    private void setupGutter(@NotNull EditorEx ed) {
        EditorGutterComponentEx gutter = ed.getGutterComponentEx();
        gutter.setPaintBackground(true);
        gutter.setInitialIconAreaWidth(ed.getLineHeight() / 4);
        gutter.setGutterPopupGroup(foldingEnablesForThisFile ? FOLDING_POPUP_ACTIONS : POPUP_ACTIONS);
        gutter.setShowDefaultGutterPopup(false);
        gutter.setCanCloseAnnotations(false);
        ed.setContextMenuGroupId(foldingEnablesForThisFile ? FOLDING_POPUP_ACTIONS_GROUP_ID : POPUP_ACTIONS_GROUP_ID);
    }

    private boolean isFoldingEnabled() {
        return state.getEnableFolding() && foldingEnablesForThisFile;
    }

    private void removeObsoleteFoldedLabels(@Nullable List<TabProvider.FoldingRegion> newRegions, @Nullable Set<String> oldFoldedLabels) {
        if (currentFilename != null && currentTab != null) {
            if (newRegions != null && !newRegions.isEmpty()) {
                if (oldFoldedLabels != null) {
                    Set<String> newLabels = newRegions.stream().map(p -> p.label).collect(Collectors.toSet());
                    oldFoldedLabels.stream().filter(l -> !newLabels.contains(l)).collect(Collectors.toSet()).forEach(label -> state.removeFoldedLabel(currentFilename, currentTab, label));
                }
            } else {
                state.clearFoldedLabels(currentFilename, currentTab);
            }
        }
    }

    private void unlessFoldingUpdatesSuppressed(Runnable runnable) {
        suppressFoldingUpdates.unlessApplied(runnable);
    }

    private void withFoldingUpdatesSuppressed(Runnable runnable) {
        suppressFoldingUpdates.apply(runnable);
    }
}
