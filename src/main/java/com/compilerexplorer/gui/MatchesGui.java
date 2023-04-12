package com.compilerexplorer.gui;

import com.compilerexplorer.common.Bundle;
import com.compilerexplorer.common.LanguageUtil;
import com.compilerexplorer.common.SuppressionFlag;
import com.compilerexplorer.common.TooltipUtil;
import com.compilerexplorer.common.component.BaseComponent;
import com.compilerexplorer.common.component.CEComponent;
import com.compilerexplorer.common.component.DataHolder;
import com.compilerexplorer.common.component.ResetFlag;
import com.compilerexplorer.datamodel.SelectedSource;
import com.compilerexplorer.datamodel.SelectedSourceCompiler;
import com.compilerexplorer.datamodel.SourceRemoteMatched;
import com.compilerexplorer.datamodel.state.CompilerMatch;
import com.compilerexplorer.datamodel.state.CompilerMatchKind;
import com.compilerexplorer.gui.json.JsonSerializer;
import com.google.gson.JsonParser;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.util.WindowStateService;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.ui.render.LabelBasedRenderer;
import com.intellij.ui.treeStructure.Tree;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.*;
import java.awt.*;
import java.util.*;
import java.util.List;

public class MatchesGui extends BaseComponent {
    @NonNls
    private static final Logger LOG = Logger.getInstance(MatchesGui.class);

    @NotNull
    private final JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0));
    @NotNull
    private final JButton button = new JButton(AllIcons.Actions.More);
    @NotNull
    private final Tree tree = new Tree();
    @NotNull
    private final SuppressionFlag suppressionFlag;
    @Nullable
    private TreeSelectionListener treeSelectionListener;
    @Nullable
    private String currentlySelectedCompilerId = null;

    public MatchesGui(@NotNull CEComponent nextComponent, @NotNull Project project, @NotNull SuppressionFlag suppressionFlag_) {
        super(nextComponent);
        LOG.debug("created");

        suppressionFlag = suppressionFlag_;

        button.setVerticalTextPosition(SwingConstants.CENTER);
        button.setHorizontalTextPosition(SwingConstants.LEFT);
        button.setMargin(null);
        int gap = button.getIconTextGap();
        Insets insets = button.getMargin();
        button.setMargin(JBUI.insets(insets.top, 0, insets.bottom, gap - insets.right));
        button.setIconTextGap(insets.right);
        button.setContentAreaFilled(false);

        tree.setRootVisible(false);
        JPanel popupPanel = new JPanel(new BorderLayout());
        popupPanel.add(new JBScrollPane(tree));

        button.addActionListener(e -> {
            JBPopupFactory.getInstance()
                    .createComponentPopupBuilder(popupPanel, null)
                    .setProject(project)
                    .setDimensionServiceKey(project, getDimensionServiceKey(), true)
                    .setResizable(true)
                    .setMovable(true)
                    .setTitle(Bundle.get("compilerexplorer.MatchesGui.PopupTitle"))
                    .createPopup()
                    .showUnderneathOf(panel);
            scrollToCurrentSelection();
        });

        panel.add(button);
    }

    @NonNls
    @Nullable
    protected String getDimensionServiceKey() {
        return WindowStateService.USE_APPLICATION_WIDE_STORE_KEY_PREFIX + ".compilerexplorer." + MatchesGui.class.getName();
    }

    @NotNull
    private static String getText(@NotNull CompilerMatch value) {
        return value.getCompilerMatchKind() != CompilerMatchKind.NO_MATCH
                ? Bundle.format("compilerexplorer.MatchesGui.TextWithMatch", "Name", value.getRemoteCompilerInfo().getName(), "MatchKind", CompilerMatchKind.asString(value.getCompilerMatchKind()))
                : value.getRemoteCompilerInfo().getName();
    }

    @NotNull
    public Component getComponent() {
        return panel;
    }

    @Override
    protected void doClear(@NotNull DataHolder data) {
        LOG.debug("doClear");
        data.remove(SourceRemoteMatched.SELECTED_KEY);
    }

    @Override
    protected void doReset() {
        LOG.debug("doReset");
        ApplicationManager.getApplication().assertIsDispatchThread();
        tree.setModel(new DefaultTreeModel(new DefaultMutableTreeNode()));
        button.setToolTipText(null);
        button.setText(null);
        button.setEnabled(false);
        currentlySelectedCompilerId = null;
    }

    @Override
    protected void doRefresh(@NotNull DataHolder data) {
        LOG.debug("doRefresh");
        suppressionFlag.apply(() -> data.get(SourceRemoteMatched.KEY).ifPresentOrElse(
                sourceRemoteMatched -> showMatches(data, sourceRemoteMatched),
                () -> {
                    LOG.debug("cannot find input");
                    doReset();
                }
        ));
    }

    private static class CompilerMatchWrapper extends CompilerMatch {
        @NotNull
        public final CompilerMatch delegate;

        public CompilerMatchWrapper(@NotNull CompilerMatch delegate_) {
            delegate = delegate_;
        }

        @Override
        @NotNull
        public String toString() {
            return getText(delegate);
        }
    }

    private void showMatches(@NotNull DataHolder data, @NotNull SourceRemoteMatched sourceRemoteMatched) {
        ApplicationManager.getApplication().assertIsDispatchThread();

        if (treeSelectionListener != null) {
            tree.removeTreeSelectionListener(treeSelectionListener);
        }
        treeSelectionListener = e -> {
            if (e.isAddedPath()) {
                Object obj = ((DefaultMutableTreeNode) e.getPath().getLastPathComponent()).getUserObject();
                if (obj instanceof CompilerMatchWrapper selectedMatchWrapper) {
                    suppressionFlag.unlessApplied(() -> ApplicationManager.getApplication().invokeLater(() -> selectCompilerMatch(ResetFlag.without(data), sourceRemoteMatched, selectedMatchWrapper.delegate, true)));
                }
            }
        };

        CompilerMatch chosenMatch = sourceRemoteMatched.getMatches().getChosenMatch();
        List<CompilerMatch> matches = sourceRemoteMatched.getMatches().getOtherMatches();
        CompilerMatch newSelection = !chosenMatch.getRemoteCompilerInfo().getId().isEmpty() ? chosenMatch : (matches.size() != 0 ? matches.get(0) : null);

        Map<String, List<CompilerMatch>> model = new HashMap<>();
        matches.forEach(match -> model.computeIfAbsent(match.getRemoteCompilerInfo().getLanguage(), unused -> new ArrayList<>()).add(match));

        DefaultMutableTreeNode rootNode = new DefaultMutableTreeNode();
        data.get(SelectedSource.KEY).ifPresent(selectedSource -> {
            @Nullable String sourceLang = model.keySet().stream().filter(lang -> lang.equalsIgnoreCase(selectedSource.getSelectedSource().language)).findFirst().orElse(null);
            if (sourceLang != null) {
                populateLangPrefixTree(rootNode, Bundle.format("compilerexplorer.MatchesGui.SourceLanguageNode", "Language", sourceLang), model.get(sourceLang));
                model.remove(sourceLang);
            }
        });
        model.entrySet().stream().sorted(Map.Entry.comparingByKey()).forEach(langEntry -> populateLangPrefixTree(rootNode, Bundle.format("compilerexplorer.MatchesGui.SimilarLanguageNode", "Language", langEntry.getKey()), langEntry.getValue()));

        tree.setModel(new DefaultTreeModel(rootNode));
        selectCompilerMatch(data, sourceRemoteMatched, newSelection, false);
        tree.addTreeSelectionListener(treeSelectionListener);

        tree.setCellRenderer(new LabelBasedRenderer.Tree() {
            @Override
            @NotNull
            public Component getTreeCellRendererComponent(@NotNull JTree tree, Object value, boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus) {
                @NotNull Component component = super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
                if (value instanceof DefaultMutableTreeNode node) {
                    Object obj = node.getUserObject();
                    if (node.getParent() != null && node.getParent().getParent() == null) {
                        String text = (String) obj;
                        String language = text.substring(text.lastIndexOf(' ') + 1);
                        setIcon(LanguageUtil.getLanguageIcon(language));
                    }
                    if (obj instanceof CompilerMatchWrapper wrapper) {
                        setToolTipText(getMatchTooltip(data, wrapper.delegate));
                    }
                }
                return component;
            }
        });

        button.setEnabled(matches.size() > 0);
    }

    private static void populateLangPrefixTree(@NotNull DefaultMutableTreeNode parentNode, String title, List<CompilerMatch> matches) {
        DefaultMutableTreeNode langNode = new DefaultMutableTreeNode();
        langNode.setUserObject(title);
        parentNode.add(langNode);

        matches.stream().sorted(Comparator.comparing(a -> a.getRemoteCompilerInfo().getName())).forEach(match -> {
            DefaultMutableTreeNode currentParent = langNode;
            StringBuilder segmentPathBuilder = new StringBuilder();
            for (String segment : splitNameIntoSegments(match.getRemoteCompilerInfo().getName())) {
                if (!segmentPathBuilder.isEmpty()) {
                    segmentPathBuilder.append(" ");
                }
                segmentPathBuilder.append(segment);
                String segmentPath = segmentPathBuilder.toString();
                DefaultMutableTreeNode existingSegmentChild = null;
                for (int i = 0; i < currentParent.getChildCount(); ++i) {
                    DefaultMutableTreeNode child = (DefaultMutableTreeNode) currentParent.getChildAt(i);
                    Object obj = child.getUserObject();
                    if (obj instanceof String existingSegment && existingSegment.equals(segmentPath)) {
                        existingSegmentChild = child;
                        break;
                    }
                }
                if (existingSegmentChild != null) {
                    currentParent = existingSegmentChild;
                } else {
                    DefaultMutableTreeNode newSegmentChild = new DefaultMutableTreeNode();
                    newSegmentChild.setUserObject(segmentPath);
                    currentParent.add(newSegmentChild);
                    currentParent = newSegmentChild;
                }
            }
            DefaultMutableTreeNode matchNode = new DefaultMutableTreeNode();
            matchNode.setUserObject(new CompilerMatchWrapper(match));
            currentParent.add(matchNode);
        });

        recursivelyPrunePrefixTree(langNode);
    }

    private static void recursivelyPrunePrefixTree(@NotNull DefaultMutableTreeNode node) {
        for (int i = 0; i < node.getChildCount(); ++i) {
            boolean pruned;
            do {
                pruned = false;
                DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getChildAt(i);
                if (child.getChildCount() == 1) {
                    DefaultMutableTreeNode grandchild = (DefaultMutableTreeNode) child.getChildAt(0);
                    node.remove(i);
                    node.insert(grandchild, i);
                    pruned = true;
                }
            } while (pruned);
            recursivelyPrunePrefixTree((DefaultMutableTreeNode) node.getChildAt(i));
        }
    }

    @NotNull
    private static List<String> splitNameIntoSegments(@NotNull String name) {
        List<String> segments = new ArrayList<>();
        StringBuilder currentSegment = null;
        int parenDepth = 0;
        for (int i = 0; i < name.length(); ++i) {
            char c = name.charAt(i);
            if (c == ' ' && parenDepth == 0) {
                if (currentSegment != null) {
                    segments.add(currentSegment.toString());
                    currentSegment = null;
                }
            } else {
                if (c == '(') {
                    ++parenDepth;
                } else if (c == ')') {
                    --parenDepth;
                }
                if (currentSegment == null) {
                    currentSegment = new StringBuilder();
                }
                currentSegment.append(c);
            }
        }
        if (currentSegment != null) {
            segments.add(currentSegment.toString());
        }
        return segments;
    }

    @Nls
    @NotNull
    private String getMatchTooltip(@NotNull DataHolder data, @NotNull CompilerMatch compilerMatch) {
        @NotNull String desiredCompiler = data.get(SelectedSourceCompiler.KEY)
                .flatMap(SelectedSourceCompiler::getLocalCompilerSettings)
                .map(compiler -> String.join(" ", compiler.getName(), compiler.getVersion(), compiler.getTarget()))
                .orElse("");
        return TooltipUtil.prettify(Bundle.format("compilerexplorer.MatchesGui.Tooltip",
                "Id", compilerMatch.getRemoteCompilerInfo().getId(),
                "Language", compilerMatch.getRemoteCompilerInfo().getLanguage(),
                "Name", compilerMatch.getRemoteCompilerInfo().getName(),
                "CompilerType", compilerMatch.getRemoteCompilerInfo().getCompilerType(),
                "Version", compilerMatch.getRemoteCompilerInfo().getVersion(),
                "MatchKind", CompilerMatchKind.asStringFull(compilerMatch.getCompilerMatchKind()),
                "RawData", prettifyJson(compilerMatch.getRemoteCompilerInfo().getRawData()),
                "DesiredCompiler", desiredCompiler));
    }

    @NonNls
    @NotNull
    private static String prettifyJson(@NonNls @NotNull String uglyJson) {
        return JsonSerializer
                .createSerializer()
                .toJson(JsonParser.parseString(uglyJson));
    }

    @Nullable
    private static TreePath recursivelyFindTreePathToMatch(@NotNull DefaultMutableTreeNode node, @NotNull String compilerMatchId) {
        for (int i = 0; i < node.getChildCount(); ++i) {
            DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getChildAt(i);
            Object obj = child.getUserObject();
            if (obj instanceof CompilerMatchWrapper matchWrapper && matchWrapper.delegate.getRemoteCompilerInfo().getId().equals(compilerMatchId)) {
                return new TreePath(child.getPath());
            }
            @Nullable TreePath path = recursivelyFindTreePathToMatch(child, compilerMatchId);
            if (path != null) {
                return path;
            }
        }
        return null;
    }

    private void selectCompilerMatch(@NotNull DataHolder data, @NotNull SourceRemoteMatched sourceRemoteMatched, @Nullable CompilerMatch compilerMatch, boolean needRefreshNext) {
        LOG.debug("selectionChanged to " + (compilerMatch != null ? compilerMatch.getRemoteCompilerInfo().getName() : null) + ", will refresh next: " + needRefreshNext);
        if (compilerMatch != null) {
            data.put(SourceRemoteMatched.SELECTED_KEY, sourceRemoteMatched.withChosenMatch(compilerMatch));
            button.setToolTipText(getMatchTooltip(data, compilerMatch));
            button.setText(getText(compilerMatch));
            currentlySelectedCompilerId = compilerMatch.getRemoteCompilerInfo().getId();
        } else {
            doClear(data);
            doReset();
        }
        if (needRefreshNext) {
            refreshNext(data);
        }
    }

    private void scrollToCurrentSelection() {
        @Nullable TreePath treePath = currentlySelectedCompilerId != null ? recursivelyFindTreePathToMatch((DefaultMutableTreeNode) tree.getModel().getRoot(), currentlySelectedCompilerId) : null;
        if (treePath != null) {
            tree.setSelectionPath(treePath);
            tree.expandPath(treePath);
            tree.scrollPathToVisible(treePath);
        }
    }
}
