package com.compilerexplorer.gui.tabs;

import com.compilerexplorer.common.*;
import com.compilerexplorer.common.component.DataHolder;
import com.compilerexplorer.datamodel.CompiledText;
import com.compilerexplorer.datamodel.PreprocessedSource;
import com.compilerexplorer.datamodel.SourceRemoteMatched;
import com.compilerexplorer.datamodel.state.Filters;
import com.compilerexplorer.datamodel.state.SettingsState;
import com.compilerexplorer.gui.*;
import com.compilerexplorer.gui.listeners.CaretPositionChangeListener;
import com.compilerexplorer.gui.listeners.FoldingChangeListener;
import com.compilerexplorer.gui.listeners.MousePopupClickListener;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.FoldRegion;
import com.intellij.openapi.editor.LogicalPosition;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.editor.colors.EditorColorsManager;
import com.intellij.openapi.editor.ex.*;
import com.intellij.openapi.editor.markup.HighlighterLayer;
import com.intellij.openapi.editor.markup.HighlighterTargetArea;
import com.intellij.openapi.editor.markup.RangeHighlighter;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.cidr.lang.asm.AsmFileType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ExplorerOutputTabProvider extends BaseExplorerUtilProvider {
    private static class EndAndSource {
        final int end;
        @NotNull
        final CompiledText.SourceLocation source;

        EndAndSource(int length_, @NotNull CompiledText.SourceLocation source_) {
            end = length_;
            source = source_;
        }
    }

    @NotNull
    private static final DefaultActionGroup gutterActions = new DefaultActionGroup(ActionManager.getInstance().getAction("compilerexplorer.AppearanceGroup"));

    @NotNull
    private final Map<CompiledText.SourceLocation, List<Range>> locationsFromSourceMap = new HashMap<>();
    @NotNull
    private final SortedMap<Integer, EndAndSource> locationsToSourceMap = new TreeMap<>();
    @NotNull
    private final Map<Integer, Integer> lineNumberToByteOffsetMap = new HashMap<>();
    @NotNull
    private final List<RangeHighlighter> highlighters = new ArrayList<>();
    @NotNull
    private final ColoredLineMarkerRenderer lineMarkerRenderer = new ColoredLineMarkerRenderer();
    @NotNull
    private final SuppressionFlag suppressFoldingUpdates = new SuppressionFlag();
    @NotNull
    private final FoldingChangeListener foldingChangeListener = new FoldingChangeListener((label, isExpanded) -> unlessFoldingUpdatesSuppressed(() -> {
        if (isExpanded) {
            getState().removeFoldedLabel(label);
        } else {
            getState().addFoldedLabel(label);
        }
    }));

    public ExplorerOutputTabProvider(@NotNull Project project) {
        super(project, Tabs.EXPLORER_OUTPUT, "compilerexplorer.ShowExplorerOutputTab", AsmFileType.INSTANCE);
    }

    @Override
    public boolean isEnabled(@NotNull DataHolder data) {
        return shouldHaveRun(data).isPresent();
    }

    @Override
    public boolean isError(@NotNull DataHolder data) {
        return compiledText(data).isEmpty();
    }

    @Override
    public void provide(@NotNull DataHolder data, @NotNull Function<String, EditorEx> textConsumer) {
        locationsFromSourceMap.clear();
        locationsToSourceMap.clear();
        lineNumberToByteOffsetMap.clear();
        highlighters.clear();

        shouldHaveRun(data).ifPresentOrElse(unusedPreprocessedText -> compiledText(data).ifPresent(compiledText -> compiledText.getCompiledResult().ifPresentOrElse(
            compiledResult -> {
                SettingsState state = getState();
                boolean shortenTemplates = state.getShortenTemplates();
                List<Range> newHighlighterRanges = new ArrayList<>();
                StringBuilder asmBuilder = new StringBuilder();
                int currentOffset = 0;
                CompiledText.SourceLocation lastChunk = new CompiledText.SourceLocation("", 0);
                int lastRangeBegin = 0;
                BiConsumer<CompiledText.SourceLocation, Range> rangeAdder = (source, range) -> {
                    newHighlighterRanges.add(range);
                    locationsFromSourceMap.computeIfAbsent(source, unused -> new ArrayList<>()).add(range);
                    locationsToSourceMap.put(range.begin, new EndAndSource(range.end, source));
                };
                int[] chunkToOffset = new int[(compiledResult.asm != null ? compiledResult.asm.size() : 0) + 1];
                int line = 0;
                for (int i = 0; compiledResult.asm != null && i < compiledResult.asm.size(); ++i) {
                    CompiledText.CompiledChunk chunk = compiledResult.asm.get(i);
                    chunkToOffset[i] = currentOffset;
                    if (chunk.opcodes != null && state.getShowOpcodes()) {
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
                            String currentChunkFile = chunk.source.file;
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
                chunkToOffset[compiledResult.asm != null ? compiledResult.asm.size() : 0] = currentOffset;

                List<Pair<String, Range>> labels = new ArrayList<>();
                if (state.getEnableFolding() && compiledResult.labelDefinitions != null) {
                    List<Pair<Integer, String>> labelEntries = compiledResult.labelDefinitions.entrySet().stream().map(e -> new Pair<>(e.getValue(), e.getKey())).sorted(Pair.comparingByFirst()).toList();
                    for (int i = 0; i < labelEntries.size(); ++i) {
                        Pair<Integer, String> entry = labelEntries.get(i);
                        int beginOffset = chunkToOffset[entry.getFirst() - 1];
                        int endOffset = (i + 1 < labelEntries.size() ? chunkToOffset[labelEntries.get(i + 1).getFirst() - 1] : currentOffset) - 1;
                        if (beginOffset < endOffset) {
                            labels.add(new Pair<>(entry.getSecond(), new Range(beginOffset, endOffset)));
                        }
                    }
                }

                provideAndGetEditor(textConsumer, asmBuilder.toString(), ed -> {
                    ed.getCaretModel().addCaretListener(new CaretPositionChangeListener(newCaretPosition -> {
                        if (getState().getAutoscrollToSource()) {
                            scrollToSource(findSourceLocationFromOffset(ed.logicalPositionToOffset(newCaretPosition)));
                        }
                    }));
                    ed.getSettings().setLineMarkerAreaShown(true);
                    setupGutter(ed);
                    updateGutter(ed);
                    updateFolding(ed);
                    ed.getFoldingModel().addListener(foldingChangeListener, DisposableParentProjectService.getInstance(project));

                    MarkupModelEx markupModel = ed.getMarkupModel();
                    markupModel.removeAllHighlighters();
                    newHighlighterRanges.forEach(range -> {
                        RangeHighlighterEx highlighter = (RangeHighlighterEx) markupModel.addRangeHighlighter(range.begin, range.end, HighlighterLayer.ADDITIONAL_SYNTAX, null, HighlighterTargetArea.LINES_IN_RANGE);
                        highlighter.setLineMarkerRenderer(lineMarkerRenderer);
                    });

                    addFolding(ed, labels, state.getFoldedLabels());
                });
            },
            () -> showExplorerError(compiledText, textConsumer)
        )), () -> textConsumer.apply("Compiler Explorer was not run"));
    }

    @NotNull
    private Optional<String> shouldHaveRun(@NotNull DataHolder data) {
        if (state.getConnected() && data.get(SourceRemoteMatched.SELECTED_KEY).isPresent()) {
            return data.get(PreprocessedSource.KEY).flatMap(PreprocessedSource::getPreprocessedText);
        } else {
            return Optional.empty();
        }
    }

    private void setupGutter(@NotNull EditorEx ed) {
        ed.getGutterComponentEx().setPaintBackground(true);
        ed.getGutterComponentEx().setInitialIconAreaWidth(ed.getLineHeight() / 4);
        ed.getGutterComponentEx().setGutterPopupGroup(null);
        ed.getGutterComponentEx().setShowDefaultGutterPopup(false);
        ed.getGutterComponentEx().setCanCloseAnnotations(false);
        ed.getGutterComponentEx().addMouseListener(new MousePopupClickListener(
                (x, y) -> showGutterPopupMenu(ed.getGutterComponentEx(), x, y)
        ));
    }

    private void showGutterPopupMenu(Component gutter, int x, int y) {
        showPopupMenu(ActionPlaces.EDITOR_GUTTER_POPUP, gutterActions, gutter, x, y);
    }

    public static void showPopupMenu(@NotNull String place, @NotNull ActionGroup actionsGroup, @NotNull Component component, int x, int y) {
        ActionManager.getInstance().createActionPopupMenu(place, actionsGroup).getComponent().show(component, x, y);
    }

    @Override
    public void updateGutter(@NotNull EditorEx ed) {
        EditorGutterComponentEx gutter = ed.getGutterComponentEx();
        gutter.setCanCloseAnnotations(true);
        gutter.closeAllAnnotations();
        gutter.setCanCloseAnnotations(false);

        SettingsState state = getState();
        Filters filters = state.getFilters();
        boolean isDisassembled = filters.getBinary() || filters.getBinaryObject();

        if (!isDisassembled && state.getShowLineNumbers()) {
            gutter.registerTextAnnotation(new LineNumberAnnotationProvider());
        }

        if (isDisassembled && state.getShowByteOffsets()) {
            gutter.registerTextAnnotation(new ByteOffsetAnnotationProvider(lineNumberToByteOffsetMap::get));
        }

        if (state.getShowSourceAnnotations()) {
            SourceAnnotationProvider provider = new SourceAnnotationProvider(this::findSourceLocationFromOffset);
            gutter.registerTextAnnotation(provider, new SourceAnnotationGutterAction(provider, ed,
                    line -> scrollToSource(findSourceLocationFromOffset(ed.logicalPositionToOffset(new LogicalPosition(line, 0))))
            ));
        }
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

    @Override
    public void updateFolding(@NotNull EditorEx ed) {
        boolean enableFolding = getState().getEnableFolding();
        ed.getFoldingModel().setFoldingEnabled(enableFolding);
        ed.getSettings().setFoldingOutlineShown(enableFolding);
    }

    private static void parseOpcodes(@NotNull StringBuilder builder, @NotNull List<String> opcodes) {
        builder.append('#');
        for (String opcode : opcodes) {
            builder.append(' ');
            builder.append(opcode);
        }
        builder.append('\n');
    }

    private static void parseChunk(@NotNull StringBuilder builder, @NotNull String text, boolean shortenTemplates) {
        if (shortenTemplates && possiblyContainsTemplates(text)) {
            TemplateShortener.shortenTemplates(builder, text);
        } else {
            builder.append(text);
        }
        builder.append('\n');
    }

    private static boolean possiblyContainsTemplates(@NotNull String text) {
        return text.indexOf('<') >= 0;
    }

    private void addFolding(@NotNull EditorEx ed, @NotNull List<Pair<String, Range>> labels, @NotNull Set<String> foldedLabels) {
        removeObsoleteFoldedLabels(labels, foldedLabels);
        FoldingModelEx foldingModel = ed.getFoldingModel();
        foldingModel.runBatchFoldingOperation(() -> withFoldingUpdatesSuppressed(() -> {
            foldingModel.clearFoldRegions();
            for (Pair<String, Range> label : labels) {
                @Nullable FoldRegion region = foldingModel.addFoldRegion(label.getSecond().begin, label.getSecond().end, label.getFirst());
                if (region != null) {
                    region.setExpanded(!state.containsFoldedLabel(label.getFirst()));
                }
            }
        }));
    }

    private void removeObsoleteFoldedLabels(@NotNull List<Pair<String, Range>> labels, @NotNull Set<String> foldedLabels) {
        Set<String> existingLabels = labels.stream().map(p -> p.getFirst()).collect(Collectors.toSet());
        foldedLabels.stream().filter(l -> !existingLabels.contains(l)).collect(Collectors.toSet()).forEach(state::removeFoldedLabel);
    }

    private void unlessFoldingUpdatesSuppressed(Runnable runnable) {
        suppressFoldingUpdates.unlessApplied(runnable);
    }

    private void withFoldingUpdatesSuppressed(Runnable runnable) {
        suppressFoldingUpdates.apply(runnable);
    }

    @Override
    public void highlightLocations(@NotNull EditorEx ed, @NotNull List<CompiledText.SourceLocation> highlightedLocations) {
        ApplicationManager.getApplication().assertIsDispatchThread();
        MarkupModelEx markupModel = ed.getMarkupModel();
        try {
            highlighters.forEach(markupModel::removeHighlighter);
        } catch (Exception e) {
            // empty
        }
        highlighters.clear();
        for (CompiledText.SourceLocation location : highlightedLocations) {
            List<Range> ranges = locationsFromSourceMap.get(location);
            if (ranges != null) {
                for (Range range : ranges) {
                    RangeHighlighter highlighter = markupModel.addRangeHighlighter(Constants.HIGHLIGHT_COLOR, range.begin, range.end, HighlighterLayer.ADDITIONAL_SYNTAX, HighlighterTargetArea.LINES_IN_RANGE);
                    highlighters.add(highlighter);
                }
            }
        }
        applyThemeColors();
    }

    @Nullable
    private Color getCurrentThemeHighlightColor() {
        return EditorColorsManager.getInstance().getGlobalScheme().getAttributes(Constants.HIGHLIGHT_COLOR).getBackgroundColor();
    }

    @Override
    public void applyThemeColors() {
        Color highlightColor = getCurrentThemeHighlightColor();
        lineMarkerRenderer.setColor(highlightColor);
        for (RangeHighlighter highlighter : highlighters) {
            highlighter.setErrorStripeMarkColor(highlightColor);
        }
    }

    @Override
    @NotNull
    public List<Range> getRangesForLocation(@NotNull CompiledText.SourceLocation location) {
        return locationsFromSourceMap.getOrDefault(location, super.getRangesForLocation(location));
    }
}
