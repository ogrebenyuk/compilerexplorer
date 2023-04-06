package com.compilerexplorer.gui.tabs;

import com.compilerexplorer.common.*;
import com.compilerexplorer.common.component.DataHolder;
import com.compilerexplorer.datamodel.CompiledText;
import com.compilerexplorer.datamodel.PreprocessedSource;
import com.compilerexplorer.datamodel.SourceRemoteMatched;
import com.compilerexplorer.datamodel.state.Filters;
import com.compilerexplorer.datamodel.state.SettingsState;
import com.compilerexplorer.gui.listeners.CaretPositionChangeListener;
import com.compilerexplorer.gui.listeners.MousePopupClickListener;
import com.compilerexplorer.gui.tabs.exploreroutput.*;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.EditorFactory;
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
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.cidr.lang.asm.AsmFileType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.function.BiConsumer;

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

    @NonNls
    @NotNull
    private static final String POPUP_ACTIONS_GROUP_ID = "compilerexplorer.ExplorerOutputEditorPopupGroup";

    @NotNull
    private final Map<CompiledText.SourceLocation, List<TextRange>> locationsFromSourceMap = new HashMap<>();
    @NotNull
    private final SortedMap<Integer, EndAndSource> locationsToSourceMap = new TreeMap<>();
    @NotNull
    private final Map<Integer, Integer> lineNumberToByteOffsetMap = new HashMap<>();
    @NotNull
    private final List<TextRange> highlighterRanges = new ArrayList<>();
    @NotNull
    private final List<RangeHighlighter> highlighters = new ArrayList<>();
    @NotNull
    private final ColoredLineMarkerRenderer lineMarkerRenderer = new ColoredLineMarkerRenderer();

    public ExplorerOutputTabProvider(@NotNull SettingsState state) {
        super(state, Tabs.EXPLORER_OUTPUT, "compilerexplorer.ShowExplorerOutputTab", AsmFileType.INSTANCE);
    }

    @Override
    public void provide(@NotNull DataHolder data, @NotNull TabContentConsumer contentConsumer) {
        shouldHaveRun(data).ifPresentOrElse(unusedPreprocessedText -> compiledText(data).ifPresent(compiledText -> compiledText.getCompiledResultIfGood().ifPresentOrElse(
            compiledResult -> contentWithFolding(true, () -> {
                clear();
                SettingsState state = getState();
                boolean shortenTemplates = state.getShortenTemplates();
                StringBuilder asmBuilder = new StringBuilder();
                int currentOffset = 0;
                CompiledText.SourceLocation lastChunk = new CompiledText.SourceLocation("", 0);
                int lastRangeBegin = 0;
                BiConsumer<CompiledText.SourceLocation, TextRange> rangeAdder = (source, range) -> {
                    highlighterRanges.add(range);
                    locationsFromSourceMap.computeIfAbsent(source, unused -> new ArrayList<>()).add(range);
                    locationsToSourceMap.put(range.getStartOffset(), new EndAndSource(range.getEndOffset(), source));
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
                                    rangeAdder.accept(new CompiledText.SourceLocation(lastChunk), new TextRange(lastRangeBegin, currentOffset - 1));
                                }
                                lastRangeBegin = currentOffset;
                                lastChunk.file = currentChunkFile;
                                lastChunk.line = chunk.source.line;
                            }
                        } else if (lastChunk.file != null && !lastChunk.file.isEmpty()) {
                            rangeAdder.accept(new CompiledText.SourceLocation(lastChunk), new TextRange(lastRangeBegin, currentOffset - 1));
                            lastChunk.file = "";
                        }
                    }
                    currentOffset = asmBuilder.length();
                }
                if (lastChunk.file != null && !lastChunk.file.isEmpty()) {
                    rangeAdder.accept(new CompiledText.SourceLocation(lastChunk), new TextRange(lastRangeBegin, currentOffset - 1));
                }
                chunkToOffset[compiledResult.asm != null ? compiledResult.asm.size() : 0] = currentOffset;

                List<TabFoldingRegion> foldingRegions = new ArrayList<>();
                if (compiledResult.labelDefinitions != null) {
                    List<Pair<Integer, String>> labelEntries = compiledResult.labelDefinitions.entrySet().stream().map(e -> new Pair<>(e.getValue(), e.getKey())).sorted(Pair.comparingByFirst()).toList();
                    for (int i = 0; i < labelEntries.size(); ++i) {
                        Pair<Integer, String> entry = labelEntries.get(i);
                        int beginOffset = chunkToOffset[entry.getFirst() - 1];
                        int endOffset = (i + 1 < labelEntries.size() ? chunkToOffset[labelEntries.get(i + 1).getFirst() - 1] : currentOffset) - 1;
                        if (beginOffset < endOffset) {
                            foldingRegions.add(new TabFoldingRegion(new TextRange(beginOffset, endOffset), entry.getSecond(), entry.getSecond()));
                        }
                    }
                }

                return new TabContent(asmBuilder.toString(), foldingRegions);
            }, contentConsumer),
            () -> {
                if (compiledText.getCanceled()) {
                    error(true, () -> {clear(); return Bundle.get("compilerexplorer.ExplorerOutputTabProvider.Canceled");}, contentConsumer);
                } else {
                    error(true, () -> {clear(); return getExplorerError(compiledText);}, contentConsumer);
                }
            }
        )), () -> message(false, () -> {clear(); return Bundle.get("compilerexplorer.ExplorerOutputTabProvider.WasNotRun");}, contentConsumer));
    }

    private void clear() {
        locationsFromSourceMap.clear();
        locationsToSourceMap.clear();
        lineNumberToByteOffsetMap.clear();
        highlighterRanges.clear();
        highlighters.clear();
    }

    @Override
    public void editorCreated(@NotNull Project project, @NotNull EditorEx ed) {
        ed.getCaretModel().addCaretListener(new CaretPositionChangeListener(newCaretPosition -> {
            if (getState().getAutoscrollToSource()) {
                scrollToSource(project, findSourceLocationFromOffset(ed.logicalPositionToOffset(newCaretPosition)));
            }
        }));
        ed.getSettings().setLineMarkerAreaShown(true);
        setupGutter(ed);
        updateGutter(project, ed);

        MarkupModelEx markupModel = ed.getMarkupModel();
        markupModel.removeAllHighlighters();
        highlighterRanges.forEach(range -> {
            RangeHighlighterEx highlighter = (RangeHighlighterEx) markupModel.addRangeHighlighter(range.getStartOffset(), range.getEndOffset(), HighlighterLayer.ADDITIONAL_SYNTAX, null, HighlighterTargetArea.LINES_IN_RANGE);
            highlighter.setLineMarkerRenderer(lineMarkerRenderer);
        });
        highlighters.clear();
    }

    @NotNull
    private Optional<String> shouldHaveRun(@NotNull DataHolder data) {
        if (getState().getConnected() && data.get(SourceRemoteMatched.SELECTED_KEY).isPresent()) {
            return data.get(PreprocessedSource.KEY).flatMap(PreprocessedSource::getPreprocessedText);
        } else {
            return Optional.empty();
        }
    }

    private void setupGutter(@NotNull EditorEx ed) {
        EditorGutterComponentEx gutter = ed.getGutterComponentEx();
        gutter.setPaintBackground(true);
        gutter.setInitialIconAreaWidth(ed.getLineHeight() / 4);
        gutter.setGutterPopupGroup(null);
        gutter.setShowDefaultGutterPopup(false);
        gutter.setCanCloseAnnotations(false);
        ed.setContextMenuGroupId(POPUP_ACTIONS_GROUP_ID);
        gutter.addMouseListener(new MousePopupClickListener(
                (x, y) -> showGutterPopupMenu(gutter, x, y)
        ));
    }

    private void showGutterPopupMenu(Component gutter, int x, int y) {
        DefaultActionGroup actions = new DefaultActionGroup(ActionManager.getInstance().getAction(POPUP_ACTIONS_GROUP_ID));
        showPopupMenu(ActionPlaces.EDITOR_GUTTER_POPUP, actions, gutter, x, y);
    }

    private static void showPopupMenu(@NotNull String place, @NotNull ActionGroup actionsGroup, @NotNull Component component, int x, int y) {
        ActionManager.getInstance().createActionPopupMenu(place, actionsGroup).getComponent().show(component, x, y);
    }

    @Override
    public void updateGutter(@NotNull Project project, @NotNull EditorEx ed) {
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
                    line -> scrollToSource(project, findSourceLocationFromOffset(ed.logicalPositionToOffset(new LogicalPosition(line, 0))))
            ));
        }
    }

    private void scrollToSource(@NotNull Project project, @Nullable CompiledText.SourceLocation source) {
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
            List<TextRange> ranges = locationsFromSourceMap.get(location);
            if (ranges != null) {
                for (TextRange range : ranges) {
                    RangeHighlighter highlighter = markupModel.addRangeHighlighter(Constants.HIGHLIGHT_COLOR, range.getStartOffset(), range.getEndOffset(), HighlighterLayer.ADDITIONAL_SYNTAX, HighlighterTargetArea.LINES_IN_RANGE);
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
    public List<TextRange> getRangesForLocation(@NotNull CompiledText.SourceLocation location) {
        return locationsFromSourceMap.getOrDefault(location, super.getRangesForLocation(location));
    }
}
