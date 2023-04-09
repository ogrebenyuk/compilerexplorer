package com.compilerexplorer.gui;

import com.compilerexplorer.common.Bundle;
import com.compilerexplorer.common.CompilerExplorerSettingsProvider;
import com.compilerexplorer.datamodel.state.EnabledRemoteLibraryInfo;
import com.compilerexplorer.datamodel.state.RemoteLibraryInfo;
import com.compilerexplorer.datamodel.state.SettingsState;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.WindowStateService;
import com.intellij.ui.*;
import com.intellij.ui.components.JBScrollPane;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import java.awt.*;
import java.util.*;
import java.util.List;

public class EnabledRemoteLibrariesGui extends DialogWrapper {
    @NotNull
    private final SettingsState state;
    @NotNull
    private final JPanel panel = new JPanel(new BorderLayout());
    @NotNull
    private final CheckedTreeNode root;

    private EnabledRemoteLibrariesGui(@NotNull Project project) {
        super(project, false, IdeModalityType.PROJECT);
        init();
        state = CompilerExplorerSettingsProvider.getInstance(project).getState();

        Map<String, List<RemoteLibraryInfo>> availableLibs = state.getRemoteLibraries();
        Map<String, List<EnabledRemoteLibraryInfo>> enabledLibs = state.getEnabledRemoteLibraries();

        root = new CheckedTreeNode();
        availableLibs.forEach((lang, libs) -> {
            @Nullable List<EnabledRemoteLibraryInfo> enabledLangLibs = enabledLibs.get(lang);
            DefaultMutableTreeNode langNode = new DefaultMutableTreeNode(lang);
            libs.sort(Comparator.comparing(RemoteLibraryInfo::getName));
            libs.forEach(lib -> {
                CheckedTreeNode libNode = new CheckedTreeNode(lib);
                @Nullable String enabledVerId = enabledLangLibs != null ? enabledLangLibs.stream().filter(enabledLib -> enabledLib.getId().equals(lib.getId())).findFirst().map(EnabledRemoteLibraryInfo::getVersionId).orElse(null) : null;
                boolean enabled = enabledVerId != null;
                libNode.setChecked(enabled);

                lib.getVersions().forEach(ver -> {
                    CheckedTreeNode verNode = new CheckedTreeNode(ver);
                    verNode.setChecked(enabledVerId != null && enabledVerId.equals(ver.getId()));
                    libNode.add(verNode);
                });

                langNode.add(libNode);
            });
            root.add(langNode);
        });

        CheckboxTree.CheckboxTreeCellRenderer renderer = new CheckboxTree.CheckboxTreeCellRenderer() {
            @Override
            public void customizeRenderer(JTree tree,
                                          Object value,
                                          boolean selected,
                                          boolean expanded,
                                          boolean leaf,
                                          int row,
                                          boolean hasFocus) {
                if (value instanceof CheckedTreeNode checkedTreeNode) {
                    Object obj = checkedTreeNode.getUserObject();
                    if (obj instanceof RemoteLibraryInfo remoteLibraryInfo) {
                        String description = remoteLibraryInfo.getDescription();
                        if (description.isEmpty()) {
                            getTextRenderer().append(Bundle.format("compilerexplorer.EnabledRemoteLibrariesGui.LibraryTextWithoutDescription", "Name", remoteLibraryInfo.getName()));
                        } else {
                            getTextRenderer().append(Bundle.format("compilerexplorer.EnabledRemoteLibrariesGui.LibraryTextWithDescription", "Name", remoteLibraryInfo.getName(), "Description", description));
                        }
                    } else if (obj instanceof RemoteLibraryInfo.Version remoteLibraryInfoVersion) {
                        getTextRenderer().append(Bundle.format("compilerexplorer.EnabledRemoteLibrariesGui.VersionText", "Id", remoteLibraryInfoVersion.getId(), "Version", remoteLibraryInfoVersion.getVersion()));
                    }
                } else if (value instanceof DefaultMutableTreeNode defaultMutableTreeNode) {
                    getTextRenderer().append(Bundle.format("compilerexplorer.EnabledRemoteLibrariesGui.LanguageText", "Language", (String) defaultMutableTreeNode.getUserObject()));
                }
            }
        };

        CheckboxTree tree = new CheckboxTree(renderer, root, new CheckboxTreeBase.CheckPolicy(false, false, false, false));

        tree.addCheckboxTreeListener(new CheckboxTreeListener() {
            @Override
            public void nodeStateChanged(@NotNull CheckedTreeNode node) {
                boolean wasAdded = node.isChecked();
                if (node.isLeaf()) {
                    CheckedTreeNode libNode = (CheckedTreeNode) node.getParent();
                    if (wasAdded) {
                        libNode.setChecked(true);
                        for (int i = 0; i < libNode.getChildCount(); ++i) {
                            CheckedTreeNode verNode = (CheckedTreeNode) libNode.getChildAt(i);
                            if (verNode != node) {
                                verNode.setChecked(false);
                            }
                        }
                    } else {
                        libNode.setChecked(false);
                    }
                } else {
                    if (wasAdded) {
                        RemoteLibraryInfo lib = (RemoteLibraryInfo) node.getUserObject();
                        DefaultMutableTreeNode langNode = (DefaultMutableTreeNode) node.getParent();
                        boolean found = false;
                        @Nullable List<EnabledRemoteLibraryInfo> enabledLangLibs = enabledLibs.get((String) langNode.getUserObject());
                        if (enabledLangLibs != null) {
                            @Nullable String enabledVerId = enabledLangLibs.stream().filter(enabledLib -> enabledLib.getId().equals(lib.getId())).map(EnabledRemoteLibraryInfo::getVersionId).findFirst().orElse(null);
                            if (enabledVerId != null) {
                                for (int i = 0; i < node.getChildCount(); ++i) {
                                    CheckedTreeNode verNode = (CheckedTreeNode) node.getChildAt(i);
                                    RemoteLibraryInfo.Version ver = (RemoteLibraryInfo.Version) verNode.getUserObject();
                                    if (ver.getId().equals(enabledVerId)) {
                                        verNode.setChecked(true);
                                        found = true;
                                        break;
                                    }
                                }
                            }
                        }
                        if (!found) {
                            CheckedTreeNode firstVer = (CheckedTreeNode) node.getChildAt(0);
                            firstVer.setChecked(true);
                        }
                    } else {
                        for (int i = 0; i < node.getChildCount(); ++i) {
                            CheckedTreeNode verNode = (CheckedTreeNode) node.getChildAt(i);
                            verNode.setChecked(false);
                        }
                    }
                }
            }
        });

        for (int i = 0; i < root.getChildCount(); i++) {
            TreeNode langNode = root.getChildAt(i);
            for (int j = 0; j < langNode.getChildCount(); ++j) {
                CheckedTreeNode libNode = (CheckedTreeNode) langNode.getChildAt(j);
                if (libNode.isChecked()) {
                    tree.expandPath(new TreePath(libNode.getPath()));
                }
            }
        }

        JBScrollPane scrollPane = new JBScrollPane(tree);
        panel.add(scrollPane);
    }

    @Override
    protected @Nullable JComponent createCenterPanel() {
        return panel;
    }

    @Override
    public boolean showAndGet() {
        boolean ok = super.showAndGet();
        if (ok) {
            Map<String, List<EnabledRemoteLibraryInfo>> enabledLangLibs = null;
            for (int i = 0; i < root.getChildCount(); ++i) {
                List<EnabledRemoteLibraryInfo> enabledLibs = null;
                DefaultMutableTreeNode langNode = (DefaultMutableTreeNode) root.getChildAt(i);
                for (int j = 0; j < langNode.getChildCount(); ++j) {
                    CheckedTreeNode libNode = (CheckedTreeNode) langNode.getChildAt(j);
                    RemoteLibraryInfo lib = (RemoteLibraryInfo) libNode.getUserObject();
                    for (int k = 0; k < libNode.getChildCount(); ++k) {
                        CheckedTreeNode verNode = (CheckedTreeNode) libNode.getChildAt(k);
                        if (verNode.isChecked()) {
                            if (enabledLibs == null) {
                                enabledLibs = new ArrayList<>();
                            }
                            EnabledRemoteLibraryInfo enabledLib = new EnabledRemoteLibraryInfo();
                            enabledLib.setId(lib.getId());
                            enabledLib.setVersionId(((RemoteLibraryInfo.Version) verNode.getUserObject()).getId());
                            enabledLibs.add(enabledLib);
                        }
                    }
                }
                if (enabledLibs != null) {
                    if (enabledLangLibs == null) {
                        enabledLangLibs = new HashMap<>();
                    }
                    enabledLangLibs.put((String) langNode.getUserObject(), enabledLibs);
                }
            }
            if (enabledLangLibs != null) {
                state.setEnabledRemoteLibraries(enabledLangLibs);
            } else {
                state.clearEnabledRemoteLibraries();
            }
        }
        return ok;
    }

    @Override
    @NonNls
    @Nullable
    protected String getDimensionServiceKey() {
        return WindowStateService.USE_APPLICATION_WIDE_STORE_KEY_PREFIX + ".compilerexplorer." + EnabledRemoteLibrariesGui.class.getName();
    }

    public static boolean show(@NotNull Project project) {
        return (new EnabledRemoteLibrariesGui(project)).showAndGet();
    }
}
