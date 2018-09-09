package com.compilerexplorer.gui;

import com.compilerexplorer.common.RecompileConsumer;
import com.compilerexplorer.common.SettingsProvider;
import com.compilerexplorer.common.datamodel.*;
import com.compilerexplorer.common.datamodel.state.*;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.fileTypes.PlainTextFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.wm.ToolWindow;
import com.intellij.openapi.wm.ex.ToolWindowEx;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.ListCellRendererWrapper;
import com.intellij.ui.components.JBTextField;
import org.jetbrains.annotations.NotNull;
import com.jetbrains.cidr.lang.asm.AsmFileType;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.border.BevelBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.basic.BasicToggleButtonUI;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.*;
import java.util.List;
import java.util.Timer;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ToolWindowGui implements ProjectSettingsConsumer, CompiledTextConsumer, SourceRemoteMatchedConsumer {
    private static final long UPDATE_DELAY_MILLIS = 1000;

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
    private SourceSettingsConsumer sourceSettingsConsumer;
    @Nullable
    private SourceRemoteMatchedConsumer sourceRemoteMatchedConsumer;
    @Nullable
    private SourceRemoteMatched sourceRemoteMatched;
    @NotNull
    private Timer updateTimer = new Timer();
    @Nullable
    private RecompileConsumer recompileConsumer;
    private boolean suppressUpdates = false;

    public ToolWindowGui(@NotNull Project project_, @NotNull ToolWindowEx toolWindow) {
        project = project_;
        content = new JPanel(new BorderLayout());

        JPanel headPanel = new JPanel();
        headPanel.setLayout(new BoxLayout(headPanel, BoxLayout.X_AXIS));

        projectSettingsComboBox = new ComboBox<>();
        projectSettingsComboBox.setToolTipText("Source to be compiled");
        projectSettingsComboBox.setRenderer(new ListCellRendererWrapper<SourceSettings>() {
            @Override
            public void customize(@Nullable JList list, @Nullable SourceSettings value, int index, boolean isSelected, boolean cellHasFocus) {
                setText((value != null) ? getText(value) : "");
            }
            @NotNull
            private String getText(@NotNull SourceSettings value) {
                return value.getSource().getPresentableName();
            }
        });
        projectSettingsComboBox.addItemListener(event -> {
            if (!suppressUpdates && event.getStateChange() == ItemEvent.SELECTED) {
                ApplicationManager.getApplication().invokeLater(() -> selectSourceSettings(projectSettingsComboBox.getItemAt(projectSettingsComboBox.getSelectedIndex())));
            }
        });
        headPanel.add(projectSettingsComboBox);

        matchesComboBox = new ComboBox<>();
        matchesComboBox.setToolTipText("Remote compiler to use");
        matchesComboBox.setRenderer(new ListCellRendererWrapper<CompilerMatch>() {
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
                ApplicationManager.getApplication().invokeLater(() -> selectCompilerMatch(matchesComboBox.getItemAt(matchesComboBox.getSelectedIndex())));
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
                    scheduleStateUpdate();
                }
            }
        });
        headPanel.add(additionalSwitchesField);

        JButton recompileButton = new JButton();
        recompileButton.setIcon(IconLoader.findIcon("/actions/refresh.png"));
        recompileButton.setToolTipText("Recompile current source");
        recompileButton.addActionListener(e -> recompile());
        headPanel.add(recompileButton);

        /*
        headPanel.add(createFilterToggleButton("11010", "Compile to binary and disassemble the output", Filters::getBinary, Filters::setBinary));
        headPanel.add(createFilterToggleButton("./a.out", "Execute the binary", Filters::getExecute, Filters::setExecute));
        headPanel.add(createFilterToggleButton(".LX0:", "Filter unused labels from the output", Filters::getLabels, Filters::setLabels));
        headPanel.add(createFilterToggleButton(".text", "Filter all assembler directives from the output", Filters::getDirectives, Filters::setDirectives));
        headPanel.add(createFilterToggleButton("//", "Remove all lines which are only comments from the output", Filters::getCommentOnly, Filters::setCommentOnly));
        headPanel.add(createFilterToggleButton("\\s+", "Trim intra-line whitespace", Filters::getTrim, Filters::setTrim));
        headPanel.add(createFilterToggleButton("Intel", "Output disassembly in Intel syntax", Filters::getIntel, Filters::setIntel));
        headPanel.add(createFilterToggleButton("Demangle", "Demangle output", Filters::getDemangle, Filters::setDemangle));
        */

        content.add(headPanel, BorderLayout.NORTH);
        JPanel mainPanel = new JPanel(new BorderLayout());
        content.add(mainPanel, BorderLayout.CENTER);
        editor = new EditorTextField(EditorFactory.getInstance().createDocument(""), project, PlainTextFileType.INSTANCE, false, false) {
            @Override
            protected EditorEx createEditor() {
                EditorEx ed = super.createEditor();
                ed.setHorizontalScrollbarVisible(true);
                ed.setVerticalScrollbarVisible(true);
                return ed;
            }
        };
        editor.setFont(new Font("monospaced", editor.getFont().getStyle(), editor.getFont().getSize()));
        mainPanel.add(editor, BorderLayout.CENTER);

        EditorFactory.getInstance().getEventMulticaster().addDocumentListener(new com.intellij.openapi.editor.event.DocumentListener() {
            @Override
            public void documentChanged(com.intellij.openapi.editor.event.DocumentEvent event) {
                if (!suppressUpdates && getState().getAutoupdateFromSource() && belongsToProject(event.getDocument())) {
                    scheduleRecompile();
                }
            }
            private boolean belongsToProject(@NotNull Document document) {
                return EditorFactory.getInstance().getEditors(document, project).length != 0;
            }
        });

        DefaultActionGroup actionGroup = new DefaultActionGroup();
        addToggleAction(actionGroup, "Compile to binary and disassemble the output", this::getFilters, Filters::getBinary, Filters::setBinary, true);
        addToggleAction(actionGroup, "Execute the binary", this::getFilters, Filters::getExecute, Filters::setExecute, true);
        addToggleAction(actionGroup, "Filter unused labels from the output", this::getFilters, Filters::getLabels, Filters::setLabels, true);
        addToggleAction(actionGroup, "Filter all assembler directives from the output", this::getFilters, Filters::getDirectives, Filters::setDirectives, true);
        addToggleAction(actionGroup, "Remove all lines which are only comments from the output", this::getFilters, Filters::getCommentOnly, Filters::setCommentOnly, true);
        addToggleAction(actionGroup, "Trim intra-line whitespace", this::getFilters, Filters::getTrim, Filters::setTrim, true);
        addToggleAction(actionGroup, "Output disassembly in Intel syntax", this::getFilters, Filters::getIntel, Filters::setIntel, true);
        addToggleAction(actionGroup, "Demangle output", this::getFilters, Filters::getDemangle, Filters::setDemangle, true);
        actionGroup.add(new Separator());
        addToggleAction(actionGroup, "Autoscroll to Source", this::getState, SettingsState::getAutoscrollToSource, SettingsState::setAutoscrollToSource, false);
        addToggleAction(actionGroup, "Autoscroll from Source", this::getState, SettingsState::getAutoscrollFromSource, SettingsState::setAutoscrollFromSource, false);
        //addToggleAction(actionGroup, "Autohighlight to Source", this::getState, SettingsState::getAutohighlightToSource, SettingsState::setAutohighlightToSource, false);
        //addToggleAction(actionGroup, "Autohighlight from Source", this::getState, SettingsState::getAutohighlightFromSource, SettingsState::setAutohighlightFromSource, false);
        addToggleAction(actionGroup, "Autoupdate from Source", this::getState, SettingsState::getAutoupdateFromSource, SettingsState::setAutoupdateFromSource, false);
        toolWindow.setAdditionalGearActions(actionGroup);
    }

    private <T> void addToggleAction(@NotNull DefaultActionGroup actionGroup, @NotNull String text, Supplier<T> supplier, Function<T, Boolean> getter, BiConsumer<T, Boolean> setter, boolean publishChange) {
        actionGroup.add(new ToggleAction(text) {
            @Override
            public boolean isSelected(AnActionEvent event) {
                return getter.apply(supplier.get());
            }
            @Override
            public void setSelected(AnActionEvent event, boolean selected) {
                if (publishChange) {
                    SettingsProvider.publishStateChangedLater(project);
                }
                setter.accept(supplier.get(), selected);
            }
        });
    }

    private void scheduleStateUpdate() {
        scheduleUpdate(() -> SettingsProvider.publishStateChangedLater(project));
    }

    private void scheduleRecompile() {
        scheduleUpdate(this::recompile);
    }

    private void recompile() {
        if (recompileConsumer != null) {
            ApplicationManager.getApplication().invokeLater(() -> recompileConsumer.recompile());
        }
    }

    private void scheduleUpdate(@NotNull Runnable runnable) {
        updateTimer.cancel();
        updateTimer = new Timer();
        updateTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                runnable.run();
            }
        }, UPDATE_DELAY_MILLIS);
    }

    @NotNull
    private JToggleButton createFilterToggleButton(@NotNull String text, @NotNull String description, Function<Filters, Boolean> getter, BiConsumer<Filters, Boolean> setter) {
        JToggleButton button = new JToggleButton(text);
        updateToggleButton(button, description, getter.apply(getFilters(false)));
        fitButtonWidthToText(button);
        button.setUI(new BasicToggleButtonUI());
        button.setRolloverEnabled(true);
        button.addItemListener(e -> {
            boolean newSelected = e.getStateChange() == ItemEvent.SELECTED;
            setter.accept(getFilters(true), newSelected);
            updateToggleButton(button, description, newSelected);
        });
        return button;
    }

    private static void fitButtonWidthToText(@NotNull AbstractButton button) {
        FontMetrics metrics = button.getFontMetrics(button.getFont());
        button.setPreferredSize(new Dimension(metrics.stringWidth(button.getText()) + 2 * button.getBorder().getBorderInsets(button).left + 2 * button.getBorder().getBorderInsets(button).right,
                button.getHeight() + 2 * button.getBorder().getBorderInsets(button).top + 2 * button.getBorder().getBorderInsets(button).bottom));
        button.setBounds(new Rectangle(button.getLocation(), button.getPreferredSize()));
    }

    private static void updateToggleButton(JToggleButton button, @NotNull String description, boolean selected) {
        button.setSelected(selected);
        button.setBorderPainted(selected);
        button.setToolTipText("[" + (selected ? "ON" : "OFF") + "] " + description);
        button.setBorder(BorderFactory.createBevelBorder(selected ? BevelBorder.LOWERED : BevelBorder.RAISED));
    }

    @NotNull
    private Filters getFilters(boolean publishChange) {
        if (publishChange) {
            SettingsProvider.publishStateChangedLater(project);
        }
        return getState().getFilters();
    }

    private void selectSourceSettings(@NotNull SourceSettings sourceSettings) {
        if (sourceSettingsConsumer != null) {
            sourceSettingsConsumer.setSourceSetting(sourceSettings);
        }
    }

    private void selectCompilerMatch(@NotNull CompilerMatch compilerMatch) {
        if (sourceRemoteMatchedConsumer != null && sourceRemoteMatched != null) {
            sourceRemoteMatchedConsumer.setSourceRemoteMatched(new SourceRemoteMatched(sourceRemoteMatched.getSourceCompilerSettings(),
                    new CompilerMatches(compilerMatch, sourceRemoteMatched.getRemoteCompilerMatches().getOtherMatches())));
        }
    }

    public void setSourceSettingsConsumer(@NotNull SourceSettingsConsumer sourceSettingsConsumer_) {
        sourceSettingsConsumer = sourceSettingsConsumer_;
    }

    public void setSourceRemoteMatchedConsumer(@NotNull SourceRemoteMatchedConsumer sourceRemoteMatchedConsumer_) {
        sourceRemoteMatchedConsumer = sourceRemoteMatchedConsumer_;
    }

    public void setRecompileConsumer(@NotNull RecompileConsumer recompileConsumer_) {
        recompileConsumer = recompileConsumer_;
    }

    @NotNull
    public JComponent getContent() {
        return content;
    }

    @Override
    public void setProjectSetting(@NotNull ProjectSettings projectSettings) {
        suppressUpdates = true;
        SourceSettings oldSelection = projectSettingsComboBox.getItemAt(projectSettingsComboBox.getSelectedIndex());
        SourceSettings newSelection = projectSettings.getSettings().stream()
                .filter(x -> oldSelection != null && x.getSource().getPath().equals(oldSelection.getSource().getPath()))
                .findFirst()
                .orElse(projectSettings.getSettings().size() != 0 ? projectSettings.getSettings().firstElement() : null);
        DefaultComboBoxModel<SourceSettings> model = new DefaultComboBoxModel<>(projectSettings.getSettings());
        model.setSelectedItem(newSelection);
        projectSettingsComboBox.setModel(model);
        if (newSelection == null) {
            projectSettingsComboBox.removeAllItems();
            showError("No source selected");
        } else if (!newSelection.equals(oldSelection)) {
            selectSourceSettings(newSelection);
        }
        suppressUpdates = false;
    }

    @Override
    public void setSourceRemoteMatched(@NotNull SourceRemoteMatched sourceRemoteMatched_) {
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
            showError("No compiler selected");
        } else {
            selectCompilerMatch(newSelection);
        }
        suppressUpdates = false;
    }

    @Override
    public void clearSourceRemoteMatched(@NotNull String reason) {
        showError(reason);
    }

    @Override
    public void setCompiledText(@NotNull CompiledText compiledText) {
        suppressUpdates = true;
        editor.setNewDocumentAndFileType(AsmFileType.INSTANCE, editor.getDocument());
        editor.setText(compiledText.getCompiledText());
        editor.setEnabled(true);
        suppressUpdates = false;
    }

    @Override
    public void clearCompiledText(@NotNull String reason) {
        showError(reason);
    }

    private void showError(@NotNull String reason) {
        suppressUpdates = true;
        editor.setNewDocumentAndFileType(PlainTextFileType.INSTANCE, editor.getDocument());
        editor.setText(filterOutTerminalEscapeSequences(reason));
        editor.setEnabled(false);
        suppressUpdates = false;
    }

    @NotNull
    private static String filterOutTerminalEscapeSequences(@NotNull String terminalText) {
        return terminalText.replaceAll("\u001B\\[[;\\d]*.", "");
    }

    @NotNull
    private SettingsState getState() {
        return SettingsProvider.getInstance(project).getState();
    }

    @NotNull
    private Filters getFilters() {
        return getState().getFilters();
    }
}

