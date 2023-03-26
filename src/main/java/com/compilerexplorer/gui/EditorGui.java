package com.compilerexplorer.gui;

import com.compilerexplorer.common.*;
import com.compilerexplorer.datamodel.*;
import com.compilerexplorer.datamodel.state.SettingsState;
import com.compilerexplorer.gui.listeners.*;
import com.compilerexplorer.gui.tracker.CaretTracker;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.*;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.ex.*;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.HighlighterTargetArea;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileTypes.PlainTextFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.EditorTextField;
import com.jetbrains.cidr.lang.asm.AsmFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.List;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class EditorGui implements Consumer<CompiledText> {
    @NotNull
    private final JPanel mainPanel;
    @NotNull
    private final Project project;
    @NotNull
    private final EditorTextField editor;
    @Nullable
    private CompiledText compiledText;
    @NotNull
    private final SuppressionFlag suppressUpdates;
    @NotNull
    private final SuppressionFlag suppressFoldingUpdates = new SuppressionFlag();
    @NotNull
    private final Map<CompiledText.SourceLocation, List<Range>> locationsFromSourceMap = new HashMap<>();
    @NotNull
    private final SortedMap<Integer, EndAndSource> locationsToSourceMap = new TreeMap<>();
    @NotNull
    private final CaretTracker caretTracker;
    @NotNull
    private final List<RangeHighlighter> highlighters = new ArrayList<>();
    @NotNull
    private final ColoredLineMarkerRenderer lineMarkerRenderer = new ColoredLineMarkerRenderer();
    @NotNull
    private final DefaultActionGroup gutterActions;
    @NotNull
    private final Map<Integer, Integer> lineNumberToByteOffsetMap = new HashMap<>();
    @NotNull
    private final CaretPositionChangeListener caretPositionChangeListener;
    @NotNull
    private final FoldingChangeListener foldingChangeListener;

    public EditorGui(@NotNull Project project_, @NotNull SuppressionFlag suppressUpdates_, @NotNull DefaultActionGroup gutterActions_) {
        project = project_;
        suppressUpdates = suppressUpdates_;
        gutterActions = gutterActions_;

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
                ed.getSettings().setLineMarkerAreaShown(true);
                ed.getCaretModel().addCaretListener(caretPositionChangeListener);
                setupGutterAnnotations(ed);
                updateGutterAnnotations(ed);
                updateFolding(ed);
                ed.getFoldingModel().addListener(foldingChangeListener, DisposableParentProjectService.getInstance(project));
                return ed;
            }
        };
        editor.setFont(new Font("monospaced", editor.getFont().getStyle(), editor.getFont().getSize()));
        mainPanel.add(editor, BorderLayout.CENTER);

        caretPositionChangeListener = new CaretPositionChangeListener(newCaretPosition -> withEditor(ed -> unlessUpdatesSuppressed(() -> {
            if (getState().getAutoscrollToSource()) {
                scrollToSource(findSourceLocationFromOffset(ed.logicalPositionToOffset(newCaretPosition)));
            }
        })));

        foldingChangeListener = new FoldingChangeListener((label, isExpanded) -> unlessFoldingUpdatesSuppressed(() -> {
            if (isExpanded) {
                getState().getFoldedLabels().remove(label);
            } else {
                getState().getFoldedLabels().add(label);
            }
        }));

        caretTracker = new CaretTracker(this::highlightLocations);
        new AllEditorsListener(project, caretTracker::update);

        project.getMessageBus().connect().subscribe(EditorColorsManager.TOPIC, new EditorColorsThemeChangeListener(this::applyThemeColors));
    }

    @NotNull
    public Component getComponent() {
        return mainPanel;
    }

    public void scrollFromSource() {
        highlightLocations(caretTracker.getLocations(), false, true);
    }

    public static void showPopupMenu(@NotNull String place, @NotNull ActionGroup actionsGroup, @NotNull Component component, int x, int y) {
        ActionManager.getInstance().createActionPopupMenu(place, actionsGroup).getComponent().show(component, x, y);
    }

    private void showGutterPopupMenu(Component gutter, int x, int y) {
        showPopupMenu(ActionPlaces.EDITOR_GUTTER_POPUP, gutterActions, gutter, x, y);
    }

    private void setupGutterAnnotations(@NotNull EditorEx ed) {
        ed.getGutterComponentEx().setPaintBackground(true);
        ed.getGutterComponentEx().setInitialIconAreaWidth(ed.getLineHeight() / 4);
        ed.getGutterComponentEx().setGutterPopupGroup(null);
        ed.getGutterComponentEx().setShowDefaultGutterPopup(false);
        ed.getGutterComponentEx().setCanCloseAnnotations(false);
        ed.getGutterComponentEx().addMouseListener(new GutterMouseClickListener(
            point -> withEditor(ed_ -> scrollToSource(findSourceLocationFromOffset(ed_.logicalPositionToOffset(ed_.xyToLogicalPosition(point))))),
            (x, y) -> withEditor(ed_ -> showGutterPopupMenu(ed_.getGutterComponentEx(), x, y))
        ));
    }

    private void withEditor(@NotNull Consumer<EditorEx> consumer) {
        @Nullable EditorEx ed = editor.getEditor(false);
        if (ed != null) {
            consumer.accept(ed);
        }
    }

    private <ReturnType> ReturnType withEditor(@NotNull Function<EditorEx, ReturnType> consumer, ReturnType defaultValue) {
        @Nullable EditorEx ed = editor.getEditor(false);
        return ed != null ? consumer.apply(ed) : defaultValue;
    }

    public void updateGutter() {
        withEditor(this::updateGutterAnnotations);
    }

    public void updateFolding() {
        withEditor(this::updateFolding);
    }

    private void updateGutterAnnotations(@NotNull EditorEx ed) {
        EditorGutterComponentEx gutter = ed.getGutterComponentEx();
        gutter.setCanCloseAnnotations(true);
        gutter.closeAllAnnotations();
        gutter.setCanCloseAnnotations(false);

        SettingsState state = getState();
        boolean showAnyAnnotations = editor.getFileType() != PlainTextFileType.INSTANCE;
        boolean isDisassembled = state.getFilters().getBinary() || state.getFilters().getBinaryObject();

        if (showAnyAnnotations && !isDisassembled && state.getShowLineNumbers()) {
            gutter.registerTextAnnotation(new LineNumberAnnotationProvider());
        }

        if (showAnyAnnotations && isDisassembled && state.getShowByteOffsets()) {
            gutter.registerTextAnnotation(new ByteOffsetAnnotationProvider(lineNumberToByteOffsetMap::get));
        }

        if (showAnyAnnotations && state.getShowSourceAnnotations()) {
            gutter.registerTextAnnotation(new SourceAnnotationProvider(this::findSourceLocationFromOffset));
        }
    }

    private void updateFolding(@NotNull EditorEx ed) {
        boolean enableFolding = getState().getEnableFolding();
        ed.getFoldingModel().setFoldingEnabled(enableFolding);
        ed.getSettings().setFoldingOutlineShown(enableFolding);
    }

    public void reparse() {
        accept(compiledText);
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

    @Override
    public void accept(CompiledText compiledText_) {
        withUpdatesSuppressed(() -> {
            ApplicationManager.getApplication().assertIsDispatchThread();
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

            int oldScrollPosition = withEditor(EditorGui::findCurrentScrollPosition, 0);

            editor.setNewDocumentAndFileType(AsmFileType.INSTANCE, editor.getDocument());
            editor.setText(asmBuilder.toString());
            editor.setEnabled(true);

            highlighters.clear();
            withEditor(ed -> {
                MarkupModelEx markupModel = ed.getMarkupModel();
                markupModel.removeAllHighlighters();
                newHighlighterRanges.forEach(range -> {
                    RangeHighlighterEx highlighter = (RangeHighlighterEx) markupModel.addRangeHighlighter(range.begin, range.end, HighlighterLayer.ADDITIONAL_SYNTAX, null, HighlighterTargetArea.LINES_IN_RANGE);
                    highlighter.setLineMarkerRenderer(lineMarkerRenderer);
                });

                scrollToPosition(ed, oldScrollPosition);

                highlightLocations(caretTracker.getLocations(), true, false);

                addFolding(ed, labels, state.getFoldedLabels());
            });
        });
    }

    public void clearCompiledText() {
        compiledText = null;
    }

    public void showError(@NotNull String reason) {
        showPlainText(reason);
    }

    private void showPlainText(@NotNull String text) {
        ApplicationManager.getApplication().assertIsDispatchThread();
        withUpdatesSuppressed(() -> {
            locationsFromSourceMap.clear();
            locationsToSourceMap.clear();
            lineNumberToByteOffsetMap.clear();

            editor.setNewDocumentAndFileType(PlainTextFileType.INSTANCE, editor.getDocument());
            editor.setText(filterOutTerminalEscapeSequences(text));
            editor.setEnabled(false);

            highlighters.clear();
        });
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

    private void unlessUpdatesSuppressed(Runnable runnable) {
        suppressUpdates.unlessApplied(runnable);
    }

    private void withUpdatesSuppressed(Runnable runnable) {
        suppressUpdates.apply(runnable);
    }

    private void unlessFoldingUpdatesSuppressed(Runnable runnable) {
        suppressFoldingUpdates.unlessApplied(runnable);
    }

    private void withFoldingUpdatesSuppressed(Runnable runnable) {
        suppressFoldingUpdates.apply(runnable);
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
        withEditor(ed -> {
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
        });
    }

    private void addFolding(@NotNull EditorEx ed, @NotNull List<Pair<String, Range>> labels, @NotNull Set<String> foldedLabels) {
        removeObsoleteFoldedLabels(labels, foldedLabels);
        FoldingModelEx foldingModel = ed.getFoldingModel();
        foldingModel.runBatchFoldingOperation(() -> withFoldingUpdatesSuppressed(() -> {
            foldingModel.clearFoldRegions();
            for (Pair<String, Range> label : labels) {
                @Nullable FoldRegion region = foldingModel.addFoldRegion(label.getSecond().begin, label.getSecond().end, label.getFirst());
                if (region != null) {
                    region.setExpanded(!foldedLabels.contains(label.getFirst()));
                }
            }
        }));
    }

    private void removeObsoleteFoldedLabels(@NotNull List<Pair<String, Range>> labels, @NotNull Set<String> foldedLabels) {
        Set<String> existingLabels = labels.stream().map(p -> p.getFirst()).collect(Collectors.toSet());
        foldedLabels.stream().filter(l -> !existingLabels.contains(l)).collect(Collectors.toSet()).forEach(foldedLabels::remove);
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
                reparse();
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
}
