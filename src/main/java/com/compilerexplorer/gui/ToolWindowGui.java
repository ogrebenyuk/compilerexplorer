package com.compilerexplorer.gui;

import com.compilerexplorer.common.*;
import com.compilerexplorer.datamodel.*;
import com.compilerexplorer.datamodel.state.*;
import com.compilerexplorer.gui.listeners.AllEditorsListener;
import com.compilerexplorer.gui.listeners.EditorChangeListener;
import com.compilerexplorer.gui.tracker.CaretTracker;
import com.intellij.icons.AllIcons;
import com.intellij.notification.NotificationType;
import com.intellij.notification.Notifications;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.editor.colors.*;
import com.intellij.openapi.editor.event.CaretEvent;
import com.intellij.openapi.editor.event.CaretListener;
import com.intellij.openapi.editor.ex.*;
import com.intellij.openapi.editor.markup.*;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileTypes.PlainTextFileType;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.wm.ex.ToolWindowEx;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.SimpleListCellRenderer;
import com.intellij.ui.components.JBTextField;
import org.jetbrains.annotations.NotNull;
import com.jetbrains.cidr.lang.asm.AsmFileType;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.lang.Error;
import java.nio.file.Paths;
import java.util.*;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ToolWindowGui {
    @NotNull
    private final Project project;
    @NotNull
    private final JPanel content;
    @NotNull
    private final ComboBox<SourceSettings> projectSettingsComboBox;
    @NotNull
    private final ComboBox<CompilerMatch> matchesComboBox;
    @NotNull
    private final EditorTextField editor;
    @Nullable
    private Consumer<SourceSettings> sourceSettingsConsumer;
    @Nullable
    private Consumer<SourceRemoteMatched> sourceRemoteMatchedConsumer;
    @Nullable
    private Consumer<PreprocessedSource> preprocessedSourceConsumer;
    @Nullable
    private Consumer<RefreshSignal> refreshSignalConsumer;
    @Nullable
    private SourceRemoteMatched sourceRemoteMatched;
    @Nullable
    private CompiledText compiledText;
    @NotNull
    private final TimerScheduler timerScheduler = new TimerScheduler();
    private boolean suppressUpdates = false;
    private boolean suppressFoldingUpdates = false;
    @NotNull
    private final Map<CompiledText.SourceLocation, List<Range>> locationsFromSourceMap = new HashMap<>();
    @NotNull
    private final SortedMap<Integer, EndAndSource> locationsToSourceMap = new TreeMap<>();
    @NotNull
    private final CaretTracker caretTracker;
    @NotNull
    private final List<RangeHighlighter> highlighters = new ArrayList<>();
    private static class ColoredLineMarkerRenderer implements LineMarkerRenderer {
        @Nullable
        private Color color;

        void setColor(@Nullable Color newColor) {
            color = newColor;
        }

        @Override
        public void paint(@NotNull Editor editor, @NotNull Graphics graphics, @NotNull Rectangle rectangle) {
            if (color == null) {
                return;
            }
            graphics.setColor(color);
            int margin = rectangle.width;
            int[] xPoints = {rectangle.x + margin, rectangle.x, rectangle.x, rectangle.x + margin};
            int[] yPoints = {rectangle.y, rectangle.y + margin, rectangle.y + rectangle.height - margin, rectangle.y + rectangle.height};
            graphics.fillPolygon(xPoints, yPoints, 4);
        }
    }
    @NotNull
    private final ColoredLineMarkerRenderer lineMarkerRenderer = new ColoredLineMarkerRenderer();
    @NotNull
    private final DefaultActionGroup gutterActions = new DefaultActionGroup();
    @NotNull
    private final Map<Integer, Integer> lineNumberToByteOffsetMap = new HashMap<>();
    @NotNull
    private static final AnAction showSettingsAction = new AnAction(Constants.PROJECT_TITLE + " Settings...") {
        @Override
        public void actionPerformed(@NotNull AnActionEvent event) {
            ShowSettingsUtil.getInstance().showSettingsDialog(event.getProject(), Constants.PROJECT_TITLE);
        }
        @Override
        public void update(@NotNull AnActionEvent event) {
            event.getPresentation().setIcon(AllIcons.General.Settings);
        }
        @Override
        public @NotNull ActionUpdateThread getActionUpdateThread() {
            return ActionUpdateThread.BGT;
        }
    };

    public ToolWindowGui(@NotNull Project project_, @NotNull ToolWindowEx toolWindow) {
        project = project_;
        content = new JPanel(new BorderLayout());

        maybeMigrateColorSettings();

        JPanel headPanel = new JPanel();
        headPanel.setLayout(new BoxLayout(headPanel, BoxLayout.X_AXIS));

        projectSettingsComboBox = new ComboBox<>();
        projectSettingsComboBox.setRenderer(new SimpleListCellRenderer<>() {
            @Override
            public void customize(@Nullable JList list, @Nullable SourceSettings value, int index, boolean isSelected, boolean cellHasFocus) {
                setText((value != null) ? getText(value) : "");
            }
            @NotNull
            private String getText(@NotNull SourceSettings value) {
                return value.getSourceName();
            }
        });
        projectSettingsComboBox.addItemListener(event -> {
            if (!suppressUpdates && event.getStateChange() == ItemEvent.SELECTED) {
                ApplicationManager.getApplication().invokeLater(() -> selectSourceSettings(projectSettingsComboBox.getItemAt(projectSettingsComboBox.getSelectedIndex())));
            }
        });
        headPanel.add(projectSettingsComboBox);

        matchesComboBox = new ComboBox<>();
        matchesComboBox.setRenderer(new SimpleListCellRenderer<>() {
            @Override
            public void customize(@Nullable JList list, @Nullable CompilerMatch value, int index, boolean isSelected, boolean cellHasFocus) {
                setText((value != null) ? getText(value) : "");
            }
            @NotNull
            private String getText(@NotNull CompilerMatch value) {
                return value.getRemoteCompilerInfo().getName() + (value.getCompilerMatchKind() != CompilerMatchKind.NO_MATCH ? " (" + CompilerMatchKind.asString(value.getCompilerMatchKind()) + ")" : "");
            }
        });
        matchesComboBox.addItemListener(event -> {
            if (!suppressUpdates && event.getStateChange() == ItemEvent.SELECTED) {
                ApplicationManager.getApplication().invokeLater(() -> selectCompilerMatchAndRecompile(matchesComboBox.getItemAt(matchesComboBox.getSelectedIndex())));
            }
        });
        headPanel.add(matchesComboBox);

        JBTextField additionalSwitchesField = new JBTextField(getState().getAdditionalSwitches());
        additionalSwitchesField.setToolTipText("Additional compiler switches");
        additionalSwitchesField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void changedUpdate(DocumentEvent e) {
                // empty
            }
            @Override
            public void removeUpdate(DocumentEvent e) {
                update();
            }
            @Override
            public void insertUpdate(DocumentEvent e) {
                update();
            }
            private void update() {
                getState().setAdditionalSwitches(additionalSwitchesField.getText());
                if (getState().getAutoupdateFromSource()) {
                    schedulePreprocess();
                }
            }
        });
        headPanel.add(additionalSwitchesField);

        JButton recompileButton = new JButton();
        recompileButton.setIcon(AllIcons.Actions.Refresh);
        recompileButton.setToolTipText("Recompile current source");
        recompileButton.addActionListener(e -> preprocess());
        Dimension minSquareRecompileSize = new Dimension();
        minSquareRecompileSize.setSize(recompileButton.getMinimumSize().getHeight(), recompileButton.getMinimumSize().getHeight());
        recompileButton.setPreferredSize(minSquareRecompileSize);
        headPanel.add(recompileButton);

        DefaultActionGroup actionGroup = new DefaultActionGroup();
        actionGroup.add(Separator.create("Output"));
        addToggleAction(actionGroup, "Compile to binary object and disassemble the output", this::getFilters, Filters::getBinaryObject, Filters::setBinaryObject, true, false, false, false);
        addToggleAction(actionGroup, "Link to binary and disassemble the output", this::getFilters, Filters::getBinary, Filters::setBinary, true, false, false, false);
        addToggleAction(actionGroup, "Execute code and show its output", this::getFilters, Filters::getExecute, Filters::setExecute, true, false, false, false);
        addToggleAction(actionGroup, "Output disassembly in Intel syntax", this::getFilters, Filters::getIntel, Filters::setIntel, true, false, false, false);
        addToggleAction(actionGroup, "Demangle output", this::getFilters, Filters::getDemangle, Filters::setDemangle, true, false, false, false);
        actionGroup.add(Separator.create());
        actionGroup.add(Separator.create("Filter"));
        addToggleAction(actionGroup, "Filter unused labels from the output", this::getFilters, Filters::getLabels, Filters::setLabels, true, false, false, false);
        addToggleAction(actionGroup, "Filter functions from other libraries from the output", this::getFilters, Filters::getLibraryCode, Filters::setLibraryCode, true, false, false, false);
        addToggleAction(actionGroup, "Filter all assembler directives from the output", this::getFilters, Filters::getDirectives, Filters::setDirectives, true, false, false, false);
        addToggleAction(actionGroup, "Remove all lines which are only comments from the output", this::getFilters, Filters::getCommentOnly, Filters::setCommentOnly, true, false, false, false);
        addToggleAction(actionGroup, "Trim intra-line whitespace", this::getFilters, Filters::getTrim, Filters::setTrim, true, false, false, false);
        actionGroup.add(Separator.create());
        actionGroup.add(Separator.create("Appearance"));
        addToggleAction(actionGroup, "Show line numbers", this::getState, SettingsState::getShowLineNumbers, SettingsState::setShowLineNumbers, false, false, true, false);
        addToggleAction(actionGroup, "Show byte offsets in disassembled output", this::getState, SettingsState::getShowByteOffsets, SettingsState::setShowByteOffsets, false, false, true, false);
        addToggleAction(actionGroup, "Show source location", this::getState, SettingsState::getShowSourceAnnotations, SettingsState::setShowSourceAnnotations, false, false, true, false);
        addToggleAction(actionGroup, "Shorten Templates", this::getState, SettingsState::getShortenTemplates, SettingsState::setShortenTemplates, false, true, false, false);
        addToggleAction(actionGroup, "Enable folding", this::getState, SettingsState::getEnableFolding, SettingsState::setEnableFolding, false, false, false, true);
        AnAction expandAllAction = createExpandAllFoldingAction("Expand all folding", true);
        actionGroup.add(expandAllAction);
        gutterActions.add(expandAllAction);
        AnAction collapseAllAction = createExpandAllFoldingAction("Collapse all folding", false);
        actionGroup.add(collapseAllAction);
        gutterActions.add(collapseAllAction);
        actionGroup.add(Separator.create());
        actionGroup.add(Separator.create("Behavior"));
        addToggleAction(actionGroup, "Autoscroll to Source", this::getState, SettingsState::getAutoscrollToSource, SettingsState::setAutoscrollToSource, false, false, false, false);
        addToggleAction(actionGroup, "Autoscroll from Source", this::getState, SettingsState::getAutoscrollFromSource, SettingsState::setAutoscrollFromSource, false, false, false, false);
        addToggleAction(actionGroup, "Autoupdate from Source", this::getState, SettingsState::getAutoupdateFromSource, SettingsState::setAutoupdateFromSource, false, false, false, false);
        actionGroup.add(showSettingsAction);

        JButton settingsButton = new JButton();
        settingsButton.setIcon(AllIcons.Actions.More);
        settingsButton.setToolTipText("Options");
        settingsButton.addActionListener(e -> ActionManager.getInstance().createActionPopupMenu(ActionPlaces.TOOLWINDOW_POPUP, actionGroup).getComponent().show(settingsButton, 0, settingsButton.getHeight()));
        Dimension minSquareSettingsSize = new Dimension();
        minSquareSettingsSize.setSize(settingsButton.getMinimumSize().getHeight(), settingsButton.getMinimumSize().getHeight());
        settingsButton.setPreferredSize(minSquareSettingsSize);
        headPanel.add(settingsButton);

        content.add(headPanel, BorderLayout.NORTH);
        JPanel mainPanel = new JPanel(new BorderLayout());
        content.add(mainPanel, BorderLayout.CENTER);
        editor = new EditorTextField(EditorFactory.getInstance().createDocument(""), project, PlainTextFileType.INSTANCE, true, false) {
            @Override
            @NotNull
            protected EditorEx createEditor() {
                EditorEx ed = super.createEditor();
                ed.setHorizontalScrollbarVisible(true);
                ed.setVerticalScrollbarVisible(true);
                ((EditorMarkupModel)ed.getMarkupModel()).setErrorStripeVisible(true);
                ed.setViewer(true);
                ed.getSettings().setLineMarkerAreaShown(true);
                ed.getCaretModel().addCaretListener(new CaretListener() {
                    @Override
                    public void caretPositionChanged(@NotNull CaretEvent event) {
                        if (!suppressUpdates && getState().getAutoscrollToSource()) {
                            scrollToSource(findSourceLocationFromOffset(ed.logicalPositionToOffset(event.getNewPosition())));
                        }
                    }
                });
                setupGutterAnnotations(ed);
                updateGutterAnnotations(ed);
                updateFolding(ed);
                ed.getFoldingModel().addListener(new FoldingListener() {
                    @Override
                    public void onFoldRegionStateChange(@NotNull FoldRegion region) {
                        FoldingListener.super.onFoldRegionStateChange(region);
                        if (!suppressFoldingUpdates) {
                            if (region.isExpanded()) {
                                getState().getFoldedLabels().remove(region.getPlaceholderText());
                            } else {
                                getState().getFoldedLabels().add(region.getPlaceholderText());
                            }
                        }
                    }
                }, DisposableParentProjectService.getInstance(project));
                project.getMessageBus().connect().subscribe(EditorColorsManager.TOPIC, (EditorColorsListener) s -> applyThemeColors());
                return ed;
            }
        };
        editor.setFont(new Font("monospaced", editor.getFont().getStyle(), editor.getFont().getSize()));
        mainPanel.add(editor, BorderLayout.CENTER);

        new EditorChangeListener(project, () ->
            ApplicationManager.getApplication().invokeLater(() -> {
                SettingsState state = getState();
                if (state.getEnabled() && state.getAutoupdateFromSource()) {
                    schedulePreprocess();
                }
            })
        , () -> this.suppressUpdates);

        actionGroup.add(new AnAction("Reset Cache and Reload") {
            @Override
            public void actionPerformed(@NotNull AnActionEvent event) {
                if (refreshSignalConsumer != null) {
                    refreshSignalConsumer.accept(RefreshSignal.RESET);
                }
            }
            @Override
            public void update(@NotNull AnActionEvent event) {
                event.getPresentation().setIcon(AllIcons.Actions.ForceRefresh);
            }
            @Override
            public @NotNull ActionUpdateThread getActionUpdateThread() {
                return ActionUpdateThread.BGT;
            }
        });

        toolWindow.setAdditionalGearActions(actionGroup);

        caretTracker = new CaretTracker(this::highlightLocations);
        new AllEditorsListener(project, caretTracker::update);

        toolWindow.setTitleActions(List.of(new AnAction("Scroll from Source") {
            @Override
            public void actionPerformed(@NotNull AnActionEvent event) {
                highlightLocations(caretTracker.getLocations(), false, true);
            }
            @Override
            public void update(@NotNull AnActionEvent event) {
                event.getPresentation().setIcon(AllIcons.General.Locate);
                event.getPresentation().setVisible(!getState().getAutoscrollFromSource());
            }
            @Override
            public @NotNull ActionUpdateThread getActionUpdateThread() {
                return ActionUpdateThread.BGT;
            }
        }));

        maybeShowInitialNotice();
    }

    private void setupGutterAnnotations(@NotNull EditorEx ed) {
        ed.getGutterComponentEx().setPaintBackground(true);
        ed.getGutterComponentEx().setInitialIconAreaWidth(ed.getLineHeight() / 4);
        ed.getGutterComponentEx().setGutterPopupGroup(null);
        ed.getGutterComponentEx().setShowDefaultGutterPopup(false);
        ed.getGutterComponentEx().setCanCloseAnnotations(false);

        ed.getGutterComponentEx().addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(@NotNull MouseEvent e) {
                maybeShowPopupMenu(e);
                if (!e.isConsumed()) {
                    scrollToSource(findSourceLocationFromOffset(ed.logicalPositionToOffset(ed.xyToLogicalPosition(e.getPoint()))));
                    e.consume();
                }
            }
            @Override
            public void mousePressed(@NotNull MouseEvent e) {
                maybeShowPopupMenu(e);
            }
            @Override
            public void mouseReleased(@NotNull MouseEvent e) {
                maybeShowPopupMenu(e);
            }
            private void maybeShowPopupMenu(@NotNull MouseEvent e) {
                if (e.isPopupTrigger()) {
                    ActionManager.getInstance().createActionPopupMenu(ActionPlaces.EDITOR_GUTTER_POPUP, gutterActions).getComponent().show(ed.getGutterComponentEx(), e.getX(), e.getY());
                    e.consume();
                }
            }
        });
    }

    static private abstract class BaseTextAnnotationGutterProvider implements TextAnnotationGutterProvider {
        @Override
        @Nullable
        public String getToolTip(int line, @Nullable Editor ed) {
            return null;
        }
        @Override
        @NotNull
        public EditorFontType getStyle(int line, @Nullable Editor ed) {
            return EditorFontType.PLAIN;
        }
        @Override
        @Nullable
        public ColorKey getColor(int line, @Nullable Editor ed) {
            return EditorColors.LINE_NUMBERS_COLOR;
        }
        @Override
        @Nullable
        public Color getBgColor(int line, @Nullable Editor ed) {
            return null;
        }
        @Override
        @Nullable
        public List<AnAction> getPopupActions(int line, @Nullable Editor ed) {
            return null;
        }
        @Override
        public void gutterClosed() {
        }
    }

    private void updateGutterAnnotations(@NotNull EditorEx ed_) {
        ed_.getGutterComponentEx().setCanCloseAnnotations(true);
        ed_.getGutterComponentEx().closeAllAnnotations();
        ed_.getGutterComponentEx().setCanCloseAnnotations(false);

        SettingsState state = getState();
        boolean showAnyAnnotations = editor.getFileType() != PlainTextFileType.INSTANCE;
        boolean isDisassembled = state.getFilters().getBinary() || state.getFilters().getBinaryObject();

        if (showAnyAnnotations && !isDisassembled && state.getShowLineNumbers()) {
            ed_.getGutterComponentEx().registerTextAnnotation(new BaseTextAnnotationGutterProvider() {
                @Override
                @NotNull
                public String getLineText(int line, @Nullable Editor ed) {
                    return String.valueOf(line + 1);
                }
            });
        }

        if (showAnyAnnotations && isDisassembled && state.getShowByteOffsets()) {
            ed_.getGutterComponentEx().registerTextAnnotation(new BaseTextAnnotationGutterProvider() {
                @Override
                @Nullable
                public String getLineText(int line, @Nullable Editor ed) {
                    Integer byteOffset = lineNumberToByteOffsetMap.get(line);
                    return byteOffset != null ? getLineText(byteOffset) : null;
                }
                @Override
                @Nullable
                public String getToolTip(int line, @Nullable Editor ed) {
                    Integer byteOffset = lineNumberToByteOffsetMap.get(line);
                    return byteOffset != null ? getTooltipText(byteOffset) : null;
                }

                @NotNull
                private String getLineText(@NotNull Integer byteOffset) {
                    return Integer.toHexString(byteOffset);
                }

                @NotNull
                private String getTooltipText(@NotNull Integer byteOffset) {
                    return "Byte offset " + byteOffset + ", hex 0x" + Integer.toHexString(byteOffset);
                }
            });
        }

        if (showAnyAnnotations && state.getShowSourceAnnotations()) {
            ed_.getGutterComponentEx().registerTextAnnotation(new BaseTextAnnotationGutterProvider() {
                @Override
                @Nullable
                public String getLineText(int line, @Nullable Editor ed) {
                    CompiledText.SourceLocation source = findSource(line, ed);
                    return source != null ? getLineText(source) : null;
                }

                @Override
                @Nullable
                public String getToolTip(int line, @Nullable Editor ed) {
                    CompiledText.SourceLocation source = findSource(line, ed);
                    return source != null ? getTooltipText(source) : null;
                }

                @Override
                @NotNull
                public EditorFontType getStyle(int line, @Nullable Editor ed) {
                    return EditorFontType.ITALIC;
                }

                @Nullable
                private CompiledText.SourceLocation findSource(int line, @Nullable Editor ed) {
                    return (ed != null) ? findSourceLocationFromOffset(ed.logicalPositionToOffset(new LogicalPosition(line, 0))) : null;
                }

                @NotNull
                private String getLineText(@NotNull CompiledText.SourceLocation source) {
                    return source.file != null ? (Paths.get(source.file).getFileName().toString() + ":" + source.line) : "";
                }

                @NotNull
                private String getTooltipText(@NotNull CompiledText.SourceLocation source) {
                    return source.file != null ? (source.file + ":" + source.line) : "";
                }
            });
        }
    }

    private void updateFolding(@NotNull EditorEx ed) {
        boolean enableFolding = getState().getEnableFolding();
        ed.getFoldingModel().setFoldingEnabled(enableFolding);
        ed.getSettings().setFoldingOutlineShown(enableFolding);
    }

    private <T> void addToggleAction(
            @NotNull DefaultActionGroup actionGroup,
            @NotNull String text,
            @NotNull Supplier<T> supplier,
            @NotNull Function<T, Boolean> getter,
            @NotNull BiConsumer<T, Boolean> setter,
            boolean recompile,
            boolean reparse,
            boolean updateGutter,
            boolean updateFolding
    ) {
        AnAction action = new ToggleAction(text) {
            @Override
            public boolean isSelected(@NotNull AnActionEvent event) {
                return getter.apply(supplier.get());
            }
            @Override
            public void setSelected(@NotNull AnActionEvent event, boolean selected) {
                setter.accept(supplier.get(), selected);
                if (recompile && refreshSignalConsumer != null) {
                    refreshSignalConsumer.accept(RefreshSignal.COMPILE);
                }
                if (reparse) {
                    reparse();
                }
                EditorEx ed = (EditorEx) editor.getEditor();
                if (updateGutter && ed != null) {
                    updateGutterAnnotations(ed);
                }
                if (updateFolding && ed != null) {
                    updateFolding(ed);
                }
            }
            @Override
            public @NotNull ActionUpdateThread getActionUpdateThread() {
                return ActionUpdateThread.BGT;
            }
        };
        actionGroup.add(action);
        if (updateGutter || updateFolding) {
            gutterActions.add(action);
        }
    }

    private void reparse() {
        asCompiledTextConsumer().accept(compiledText);
    }

    private void scrollToSource(@Nullable CompiledText.SourceLocation source) {
        if (source != null && source.file != null) {
            try {
                VirtualFile file = LocalFileSystem.getInstance().findFileByPath(source.file);
                if (file != null) {
                    FileEditorManager.getInstance(project).openFile(file, true);
                    Arrays.stream(EditorFactory.getInstance().getAllEditors())
                            .filter(editor -> {
                                if (editor.getProject() == project) {
                                    VirtualFile f = FileDocumentManager.getInstance().getFile(editor.getDocument());
                                    return f != null && PathNormalizer.normalizePath(file.getPath()).equals(PathNormalizer.normalizePath(f.getPath()));
                                }
                                return false;
                            })
                            .forEach(editor -> {
                                editor.getCaretModel().moveToLogicalPosition(new LogicalPosition(source.line - 1, 0));
                                editor.getScrollingModel().scrollToCaret(ScrollType.CENTER);
                            });
                }
            } catch (Exception e) {
                // empty
            }
        }
    }

    private void schedulePreprocess() {
        timerScheduler.schedule(this::preprocess, getState().getDelayMillis());
    }

    private void preprocess() {
        if (refreshSignalConsumer != null) {
            ApplicationManager.getApplication().invokeLater(() -> refreshSignalConsumer.accept(RefreshSignal.PREPROCESS));
        }
    }

    private void selectSourceSettings(@NotNull SourceSettings sourceSettings) {
        projectSettingsComboBox.setToolTipText(getSourceTooltip(sourceSettings));
        if (sourceSettingsConsumer != null) {
            sourceSettingsConsumer.accept(sourceSettings);
        }
    }

    @NotNull
    private String getSourceTooltip(@NotNull SourceSettings sourceSettings) {
        return "File: " + sourceSettings.getSourcePath()
                + "<br/>Language: " + sourceSettings.getLanguage()
                + "<br/>Compiler: " + sourceSettings.getCompilerPath()
                + "<br/>Compiler kind: " + sourceSettings.getCompilerKind()
                + "<br/>Compiler options: " + String.join(" ", sourceSettings.getSwitches());
    }

    private void selectCompilerMatch(@NotNull CompilerMatch compilerMatch) {
        matchesComboBox.setToolTipText(getMatchTooltip(compilerMatch));
        if (sourceRemoteMatchedConsumer != null && sourceRemoteMatched != null) {
            sourceRemoteMatchedConsumer.accept(new SourceRemoteMatched(sourceRemoteMatched.getSourceCompilerSettings(),
                    new CompilerMatches(compilerMatch, sourceRemoteMatched.getRemoteCompilerMatches().getOtherMatches())));
        }
    }

    private void selectCompilerMatchAndRecompile(@NotNull CompilerMatch compilerMatch) {
        matchesComboBox.setToolTipText(getMatchTooltip(compilerMatch));
        if (preprocessedSourceConsumer != null && compiledText != null) {
            preprocessedSourceConsumer.accept(
                    new PreprocessedSource(
                            new SourceRemoteMatched(
                                    compiledText.getPreprocessedSource().getSourceRemoteMatched().getSourceCompilerSettings(),
                                    new CompilerMatches(
                                            compilerMatch,
                                            compiledText.getPreprocessedSource().getSourceRemoteMatched().getRemoteCompilerMatches().getOtherMatches()
                                    )
                            ),
                            compiledText.getPreprocessedSource().getPreprocessedText()
                    )
            );
        } else {
            selectCompilerMatch(compilerMatch);
        }
    }

    @NotNull
    private String getMatchTooltip(@NotNull CompilerMatch compilerMatch) {
        return "Id: " + compilerMatch.getRemoteCompilerInfo().getId()
                + "<br/>Language: " + compilerMatch.getRemoteCompilerInfo().getLanguage()
                + "<br/>Name: " + compilerMatch.getRemoteCompilerInfo().getName()
                + "<br/>Version: " + compilerMatch.getRemoteCompilerInfo().getVersion()
                + "<br/>Match kind: " + CompilerMatchKind.asStringFull(compilerMatch.getCompilerMatchKind())
                + "<br/>Raw data: " + compilerMatch.getRemoteCompilerInfo().getRawData();
    }

    public void setSourceSettingsConsumer(@NotNull Consumer<SourceSettings> sourceSettingsConsumer_) {
        sourceSettingsConsumer = sourceSettingsConsumer_;
    }

    public void setSourceRemoteMatchedConsumer(@NotNull Consumer<SourceRemoteMatched> sourceRemoteMatchedConsumer_) {
        sourceRemoteMatchedConsumer = sourceRemoteMatchedConsumer_;
    }

    public void setPreprocessedSourceConsumer(@NotNull Consumer<PreprocessedSource> preprocessedSourceConsumer_) {
        preprocessedSourceConsumer = preprocessedSourceConsumer_;
    }

    public void setRefreshSignalConsumer(@NotNull Consumer<RefreshSignal> refreshSignalConsumer_) {
        refreshSignalConsumer = refreshSignalConsumer_;
    }

    @NotNull
    public JComponent getContent() {
        return content;
    }

    @NotNull
    public Consumer<RefreshSignal> asResetSignalConsumer() {
        return refreshSignal -> {
            ApplicationManager.getApplication().assertIsDispatchThread();
            projectSettingsComboBox.removeAllItems();
            projectSettingsComboBox.setToolTipText("");
        };
    }

    @NotNull
    public Consumer<RefreshSignal> asReconnectSignalConsumer() {
        return refreshSignal -> {
            ApplicationManager.getApplication().assertIsDispatchThread();
            matchesComboBox.removeAllItems();
            matchesComboBox.setToolTipText("");
        };
    }

    @NotNull
    public Consumer<RefreshSignal> asRecompileSignalConsumer() {
        return refreshSignal -> {
            ApplicationManager.getApplication().assertIsDispatchThread();
            sourceRemoteMatched = null;
            compiledText = null;
            //showError("");
        };
    }

    @NotNull
    public Consumer<ProjectSettings> asProjectSettingsConsumer() {
        return projectSettings -> {
            ApplicationManager.getApplication().assertIsDispatchThread();
            suppressUpdates = true;
            SourceSettings oldSelection = projectSettingsComboBox.getItemAt(projectSettingsComboBox.getSelectedIndex());
            SourceSettings newSelection = projectSettings.getSettings().stream()
                    .filter(x -> oldSelection != null && x.getSourcePath().equals(oldSelection.getSourcePath()))
                    .findFirst()
                    .orElse(projectSettings.getSettings().size() != 0 ? projectSettings.getSettings().firstElement() : null);
            DefaultComboBoxModel<SourceSettings> model = new DefaultComboBoxModel<>(projectSettings.getSettings());
            model.setSelectedItem(newSelection);
            projectSettingsComboBox.setModel(model);
            if (newSelection == null) {
                projectSettingsComboBox.removeAllItems();
                projectSettingsComboBox.setToolTipText("");
                showError("No source selected");
            } else {
                selectSourceSettings(newSelection);
            }
            suppressUpdates = false;
        };
    }

    @NotNull
    public Consumer<SourceRemoteMatched> asSourceRemoteMatchedConsumer() {
        return sourceRemoteMatched_ -> {
            ApplicationManager.getApplication().assertIsDispatchThread();
            suppressUpdates = true;
            sourceRemoteMatched = sourceRemoteMatched_;
            CompilerMatch chosenMatch = sourceRemoteMatched.getRemoteCompilerMatches().getChosenMatch();
            List<CompilerMatch> matches = sourceRemoteMatched.getRemoteCompilerMatches().getOtherMatches();
            CompilerMatch oldSelection = matchesComboBox.getItemAt(matchesComboBox.getSelectedIndex());
            CompilerMatch newSelection = matches.stream()
                    .filter(x -> oldSelection != null && x.getRemoteCompilerInfo().getId().equals(oldSelection.getRemoteCompilerInfo().getId()))
                    .findFirst()
                    .orElse(!chosenMatch.getRemoteCompilerInfo().getId().isEmpty() ? chosenMatch : (matches.size() != 0 ? matches.get(0) : null));
            DefaultComboBoxModel<CompilerMatch> model = new DefaultComboBoxModel<>(
                    matches.stream()
                            .map(x -> newSelection == null || !newSelection.getRemoteCompilerInfo().getId().equals(x.getRemoteCompilerInfo().getId()) ? x : newSelection)
                            .filter(Objects::nonNull).collect(Collectors.toCollection(Vector::new)));
            model.setSelectedItem(newSelection);
            matchesComboBox.setModel(model);
            if (newSelection == null) {
                matchesComboBox.removeAllItems();
                matchesComboBox.setToolTipText("");
                showError("No compiler selected");
            } else {
                selectCompilerMatch(newSelection);
            }
            suppressUpdates = false;
        };
    }

    @NotNull
    public Consumer<CompiledText> asCompiledTextConsumer() {
        return compiledText_ -> {
            ApplicationManager.getApplication().assertIsDispatchThread();
            suppressUpdates = true;
            compiledText = compiledText_;
            if (compiledText == null) {
                return;
            }

            if (compiledText.getCompiledResult().execResult != null) {
                StringBuilder execResultBuilder = new StringBuilder();
                for (CompiledText.CompiledChunk chunk : compiledText.getCompiledResult().execResult.stdout) {
                    if (chunk.text != null) {
                        execResultBuilder.append(chunk.text);
                        execResultBuilder.append('\n');
                    }
                }
                showPlainText(execResultBuilder.toString());
                return;
            }

            SettingsState state = getState();
            boolean shortenTemplates = state.getShortenTemplates();
            List<Range> newHighlighterRanges = new ArrayList<>();
            locationsFromSourceMap.clear();
            locationsToSourceMap.clear();
            lineNumberToByteOffsetMap.clear();
            StringBuilder asmBuilder = new StringBuilder();
            int currentOffset = 0;
            CompiledText.SourceLocation lastChunk = new CompiledText.SourceLocation("", 0);
            int lastRangeBegin = 0;
            BiConsumer<CompiledText.SourceLocation, Range> rangeAdder = (source, range) -> {
                newHighlighterRanges.add(range);
                locationsFromSourceMap.computeIfAbsent(source, unused -> new ArrayList<>()).add(range);
                locationsToSourceMap.put(range.begin, new EndAndSource(range.end, source));
            };
            int [] chunkToOffset = new int[compiledText.getCompiledResult().asm.size() + 1];
            int line = 0;
            for (int i = 0; i < compiledText.getCompiledResult().asm.size(); ++i) {
                CompiledText.CompiledChunk chunk = compiledText.getCompiledResult().asm.get(i);
                chunkToOffset[i] = currentOffset;
                if (chunk.opcodes != null) {
                    parseOpcodes(asmBuilder, chunk.opcodes);
                    ++line;
                }
                if (chunk.address != CompiledText.CompiledChunk.NO_ADDRESS) {
                    lineNumberToByteOffsetMap.put(line, chunk.address);
                }
                if (chunk.text != null) {
                    parseChunk(asmBuilder, chunk.text, shortenTemplates);
                    ++line;
                    if (chunk.source != null && chunk.source.file != null) {
                        String currentChunkFile = PathNormalizer.normalizePath(new File(chunk.source.file).getAbsolutePath());
                        if ((!currentChunkFile.equals(lastChunk.file)) || (chunk.source.line != lastChunk.line)) {
                            if (lastChunk.file != null && !lastChunk.file.isEmpty()) {
                                rangeAdder.accept(new CompiledText.SourceLocation(lastChunk), new Range(lastRangeBegin, currentOffset - 1));
                            }
                            lastRangeBegin = currentOffset;
                            lastChunk.file = currentChunkFile;
                            lastChunk.line = chunk.source.line;
                        }
                    } else if (lastChunk.file != null && !lastChunk.file.isEmpty()) {
                        rangeAdder.accept(new CompiledText.SourceLocation(lastChunk), new Range(lastRangeBegin, currentOffset - 1));
                        lastChunk.file = "";
                    }
                }
                currentOffset = asmBuilder.length();
            }
            if (lastChunk.file != null && !lastChunk.file.isEmpty()) {
                rangeAdder.accept(new CompiledText.SourceLocation(lastChunk), new Range(lastRangeBegin, currentOffset - 1));
            }
            chunkToOffset[compiledText.getCompiledResult().asm.size()] = currentOffset;

            List<Pair<String, Range>> labels = new ArrayList<>();
            if (state.getEnableFolding() && compiledText.getCompiledResult().labelDefinitions != null) {
                List<Pair<Integer, String>> labelEntries = compiledText.getCompiledResult().labelDefinitions.entrySet().stream().map(e -> new Pair<>(e.getValue(), e.getKey())).sorted(Pair.comparingByFirst()).toList();
                for (int i = 0; i < labelEntries.size(); ++i) {
                    Pair<Integer, String> entry = labelEntries.get(i);
                    int beginOffset = chunkToOffset[entry.getFirst() - 1];
                    int endOffset = (i + 1 < labelEntries.size() ? chunkToOffset[labelEntries.get(i + 1).getFirst() - 1] : currentOffset) - 1;
                    if (beginOffset < endOffset) {
                        labels.add(new Pair<>(entry.getSecond(), new Range(beginOffset, endOffset)));
                    }
                }
            }

            EditorEx oldEd = (EditorEx) editor.getEditor();
            int oldScrollPosition = (oldEd != null) ? findCurrentScrollPosition(oldEd) : 0;

            editor.setNewDocumentAndFileType(AsmFileType.INSTANCE, editor.getDocument());
            editor.setText(asmBuilder.toString());
            editor.setEnabled(true);

            highlighters.clear();
            EditorEx ed = (EditorEx) editor.getEditor();
            if (ed != null) {
                MarkupModelEx markupModel = ed.getMarkupModel();
                markupModel.removeAllHighlighters();
                newHighlighterRanges.forEach(range -> {
                    RangeHighlighterEx highlighter = (RangeHighlighterEx) markupModel.addRangeHighlighter(range.begin, range.end, HighlighterLayer.ADDITIONAL_SYNTAX, null, HighlighterTargetArea.LINES_IN_RANGE);
                    highlighter.setLineMarkerRenderer(lineMarkerRenderer);
                });

                scrollToPosition(ed, oldScrollPosition);

                highlightLocations(caretTracker.getLocations(), true, false);

                addFolding(ed, labels, state.getFoldedLabels());
            }

            suppressUpdates = false;
        };
    }

    @NotNull
    public Consumer<Error> asErrorConsumer() {
        return error -> showError(error.getMessage());
    }

    private void showError(@NotNull String reason) {
        showPlainText(reason);
    }

    private void showPlainText(@NotNull String text) {
        ApplicationManager.getApplication().assertIsDispatchThread();
        suppressUpdates = true;
        locationsFromSourceMap.clear();
        locationsToSourceMap.clear();
        lineNumberToByteOffsetMap.clear();

        editor.setNewDocumentAndFileType(PlainTextFileType.INSTANCE, editor.getDocument());
        editor.setText(filterOutTerminalEscapeSequences(text));
        editor.setEnabled(false);

        highlighters.clear();
        suppressUpdates = false;
    }

    private static void parseChunk(@NotNull StringBuilder builder, @NotNull String text, boolean shortenTemplates) {
        if (shortenTemplates && possiblyContainsTemplates(text)) {
            TemplateShortener.shortenTemplates(builder, text);
        } else {
            builder.append(text);
        }
        builder.append('\n');
    }

    private static void parseOpcodes(@NotNull StringBuilder builder, @NotNull List<String> opcodes) {
        builder.append('#');
        for (String opcode : opcodes) {
            builder.append(' ');
            builder.append(opcode);
        }
        builder.append('\n');
    }

    private static boolean possiblyContainsTemplates(@NotNull String text) {
        return text.indexOf('<') >= 0;
    }

    @NotNull
    private static String filterOutTerminalEscapeSequences(@NotNull String terminalText) {
        return terminalText.replaceAll("\u001B\\[[;\\d]*.", "");
    }

    @NotNull
    private SettingsState getState() {
        return CompilerExplorerSettingsProvider.getInstance(project).getState();
    }

    @NotNull
    private Filters getFilters() {
        return getState().getFilters();
    }

    @Nullable
    private Color getCurrentThemeHighlightColor() {
        return EditorColorsManager.getInstance().getGlobalScheme().getAttributes(Constants.HIGHLIGHT_COLOR).getBackgroundColor();
    }

    private void applyThemeColors() {
        Color highlightColor = getCurrentThemeHighlightColor();
        lineMarkerRenderer.setColor(highlightColor);
        for (RangeHighlighter highlighter : highlighters) {
            highlighter.setErrorStripeMarkColor(highlightColor);
        }
    }

    private void highlightLocations(@NotNull List<CompiledText.SourceLocation> locations) {
        highlightLocations(locations, true, false);
    }

    private void highlightLocations(@NotNull List<CompiledText.SourceLocation> locations, boolean highlight, boolean forceScroll) {
        ApplicationManager.getApplication().assertIsDispatchThread();
        EditorEx ed = (EditorEx) editor.getEditor();
        if (ed == null) {
            return;
        }

        SettingsState state = getState();
        boolean scroll = forceScroll || state.getAutoscrollFromSource();
        int currentScrollPosition = scroll ? findCurrentScrollPosition(ed) : -1;
        int closestPosition = -1;
        int closestPositionDistance = -1;

        MarkupModelEx markupModel = ed.getMarkupModel();
        if (highlight) {
            try {
                highlighters.forEach(markupModel::removeHighlighter);
            } catch (Exception e) {
                // empty
            }
            highlighters.clear();
        }
        for (CompiledText.SourceLocation location : locations) {
            List<Range> ranges = locationsFromSourceMap.get(location);
            if (ranges != null) {
                for (Range range : ranges) {
                    if (highlight) {
                        RangeHighlighter highlighter = markupModel.addRangeHighlighter(Constants.HIGHLIGHT_COLOR, range.begin, range.end, HighlighterLayer.ADDITIONAL_SYNTAX, HighlighterTargetArea.LINES_IN_RANGE);
                        highlighters.add(highlighter);
                    }
                    if (scroll) {
                        int positionBegin = ed.offsetToXY(range.begin).y;
                        int diffBegin = Math.abs(positionBegin - currentScrollPosition);
                        if ((closestPositionDistance < 0) || (diffBegin < closestPositionDistance)) {
                            closestPositionDistance = diffBegin;
                            closestPosition = positionBegin;
                        }
                        int positionEnd = ed.offsetToXY(range.end).y + ed.getLineHeight();
                        int diffEnd = Math.abs(positionEnd - currentScrollPosition);
                        if ((closestPositionDistance < 0) || (diffEnd < closestPositionDistance)) {
                            closestPositionDistance = diffEnd;
                            closestPosition = positionEnd;
                        }
                    }
                }
            }
        }
        if (highlight) {
            applyThemeColors();
        }

        if (scroll && (closestPosition >= 0)) {
            scrollToPosition(ed, closestPosition - (ed.getScrollingModel().getVisibleAreaOnScrollingFinished().height / 2));
        }
    }

    private void addFolding(@NotNull EditorEx ed, @NotNull List<Pair<String, Range>> labels, @NotNull Set<String> foldedLabels) {
        removeObsoleteFoldedLabels(labels, foldedLabels);
        FoldingModelEx foldingModel = ed.getFoldingModel();
        foldingModel.runBatchFoldingOperation(() -> {
            suppressFoldingUpdates = true;
            foldingModel.clearFoldRegions();
            for (Pair<String, Range> label : labels) {
                @Nullable FoldRegion region = foldingModel.addFoldRegion(label.getSecond().begin, label.getSecond().end, label.getFirst());
                if (region != null) {
                    region.setExpanded(!foldedLabels.contains(label.getFirst()));
                }
            }
            suppressFoldingUpdates = false;
        });
    }

    private void removeObsoleteFoldedLabels(@NotNull List<Pair<String, Range>> labels, @NotNull Set<String> foldedLabels) {
        Set<String> existingLabels = labels.stream().map(p -> p.getFirst()).collect(Collectors.toSet());
        foldedLabels.stream().filter(l -> !existingLabels.contains(l)).collect(Collectors.toSet()).forEach(foldedLabels::remove);
    }

    private void expandAllFolding(boolean isExpanded) {
        EditorEx ed = (EditorEx) editor.getEditor();
        if (ed != null && getState().getEnableFolding()) {
            FoldingModelEx foldingModel = ed.getFoldingModel();
            foldingModel.runBatchFoldingOperation(() -> {
                for (FoldRegion region : foldingModel.getAllFoldRegions()) {
                    region.setExpanded(isExpanded);
                }
            });
            reparse();
        }
    }

    @NotNull
    AnAction createExpandAllFoldingAction(@NotNull String text, boolean isExpanded) {
        return new AnAction(text) {
            @Override
            public void actionPerformed(@NotNull AnActionEvent e) {
                expandAllFolding(isExpanded);
            }
            @Override
            public void update(@NotNull AnActionEvent event) {
                event.getPresentation().setEnabled(getState().getEnableFolding());
            }
            @Override
            public @NotNull ActionUpdateThread getActionUpdateThread() {
                return ActionUpdateThread.BGT;
            }
        };
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

    @Nullable
    private CompiledText.SourceLocation findSourceLocationFromOffset(int offset) {
        SortedMap<Integer, EndAndSource> headMap = locationsToSourceMap.headMap(offset + 1);
        if (!headMap.isEmpty()) {
            EndAndSource lastValue = headMap.get(headMap.lastKey());
            if (lastValue.end >= offset) {
                return lastValue.source;
            }
        }
        return null;
    }

    private static class Range {
        final int begin;
        final int end;

        Range(int begin_, int end_) {
            begin = begin_;
            end = end_;
        }
    }

    private static class EndAndSource {
        final int end;
        @NotNull
        final CompiledText.SourceLocation source;

        EndAndSource(int length_, @NotNull CompiledText.SourceLocation source_) {
            end = length_;
            source = source_;
        }
    }

    private void maybeShowInitialNotice() {
        if (!getState().getInitialNoticeShown()) {
            showInitialNotice();
            getState().setInitialNoticeShown(true);
        }
    }

    private void showInitialNotice() {
        Notifications.Bus.notify(Constants.NOTIFICATION_GROUP.createNotification(Constants.INITIAL_NOTICE, NotificationType.INFORMATION).addAction(showSettingsAction), project);
    }

    private void maybeMigrateColorSettings() {
        if (getState().getHighlightColorRGB() != SettingsState.NO_SAVED_COLOR) {
            migrateColorSettings();
            getState().setHighlightColorRGB(SettingsState.NO_SAVED_COLOR);
        }
    }

    private void migrateColorSettings() {
        TextAttributes migrated = new TextAttributes();
        migrated.copyFrom(EditorColorsManager.getInstance().getGlobalScheme().getAttributes(Constants.HIGHLIGHT_COLOR));
        migrated.setBackgroundColor(new Color(getState().getHighlightColorRGB()));
        EditorColorsManager.getInstance().getGlobalScheme().setAttributes(Constants.HIGHLIGHT_COLOR, migrated);
    }
}
