package com.compilerexplorer.gui;

import com.compilerexplorer.common.RefreshSignal;
import com.compilerexplorer.common.SettingsProvider;
import com.compilerexplorer.common.datamodel.*;
import com.compilerexplorer.common.datamodel.state.*;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.fileTypes.PlainTextFileType;
import com.intellij.openapi.options.ShowSettingsUtil;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.openapi.util.IconLoader;
import com.intellij.openapi.wm.ex.ToolWindowEx;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.ListCellRendererWrapper;
import com.intellij.ui.components.JBTextField;
import org.jetbrains.annotations.NotNull;
import com.jetbrains.cidr.lang.asm.AsmFileType;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.lang.Error;
import java.util.*;
import java.util.List;
import java.util.Timer;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public class ToolWindowGui {
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
    private Consumer<SourceSettings> sourceSettingsConsumer;
    @Nullable
    private Consumer<SourceRemoteMatched> sourceRemoteMatchedConsumer;
    @Nullable
    private Consumer<RefreshSignal> refreshSignalConsumer;
    @Nullable
    private SourceRemoteMatched sourceRemoteMatched;
    @NotNull
    private Timer updateTimer = new Timer();
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
                    schedulePreprocess();
                }
            }
        });
        headPanel.add(additionalSwitchesField);

        JButton recompileButton = new JButton();
        recompileButton.setIcon(IconLoader.findIcon("/actions/refresh.png"));
        recompileButton.setToolTipText("Recompile current source");
        recompileButton.addActionListener(e -> preprocess());
        headPanel.add(recompileButton);

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
                    schedulePreprocess();
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
        addToggleAction(actionGroup, "Autoupdate from Source", this::getState, SettingsState::getAutoupdateFromSource, SettingsState::setAutoupdateFromSource, false);

        actionGroup.add(new AnAction("Compiler Explorer Settings...", null, AllIcons.General.Settings) {
            @Override
            public void actionPerformed(AnActionEvent event) {
                ShowSettingsUtil.getInstance().showSettingsDialog(project, "Compiler Explorer");
            }
            @Override
            public void update(AnActionEvent event) {
                event.getPresentation().setIcon(getTemplatePresentation().getIcon());
            }
        });

        actionGroup.add(new AnAction("Reset Cache and Reload", "", AllIcons.Actions.ForceRefresh) {
            @Override
            public void actionPerformed(AnActionEvent event) {
                if (refreshSignalConsumer != null) {
                    refreshSignalConsumer.accept(RefreshSignal.RESET);
                }
            }
            @Override
            public void update(AnActionEvent event) {
                event.getPresentation().setIcon(getTemplatePresentation().getIcon());
            }
        });

        toolWindow.setAdditionalGearActions(actionGroup);
    }

    private <T> void addToggleAction(@NotNull DefaultActionGroup actionGroup, @NotNull String text, Supplier<T> supplier, Function<T, Boolean> getter, BiConsumer<T, Boolean> setter, boolean recompile) {
        actionGroup.add(new ToggleAction(text) {
            @Override
            public boolean isSelected(AnActionEvent event) {
                return getter.apply(supplier.get());
            }
            @Override
            public void setSelected(AnActionEvent event, boolean selected) {
                setter.accept(supplier.get(), selected);
                if (recompile && refreshSignalConsumer != null) {
                    refreshSignalConsumer.accept(RefreshSignal.COMPILE);
                }
            }
        });
    }

    private void schedulePreprocess() {
        scheduleUpdate(this::preprocess);
    }

    private void preprocess() {
        if (refreshSignalConsumer != null) {
            ApplicationManager.getApplication().invokeLater(() -> refreshSignalConsumer.accept(RefreshSignal.PREPROCESS));
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

    private void selectSourceSettings(@NotNull SourceSettings sourceSettings) {
        if (sourceSettingsConsumer != null) {
            sourceSettingsConsumer.accept(sourceSettings);
        }
    }

    private void selectCompilerMatch(@NotNull CompilerMatch compilerMatch) {
        if (sourceRemoteMatchedConsumer != null && sourceRemoteMatched != null) {
            sourceRemoteMatchedConsumer.accept(new SourceRemoteMatched(sourceRemoteMatched.getSourceCompilerSettings(),
                    new CompilerMatches(compilerMatch, sourceRemoteMatched.getRemoteCompilerMatches().getOtherMatches())));
        }
    }

    public void setSourceSettingsConsumer(@NotNull Consumer<SourceSettings> sourceSettingsConsumer_) {
        sourceSettingsConsumer = sourceSettingsConsumer_;
    }

    public void setSourceRemoteMatchedConsumer(@NotNull Consumer<SourceRemoteMatched> sourceRemoteMatchedConsumer_) {
        sourceRemoteMatchedConsumer = sourceRemoteMatchedConsumer_;
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
            System.out.println("ToolWindowGui::asResetSignalConsumer");
            projectSettingsComboBox.removeAllItems();
        };
    }

    @NotNull
    public Consumer<RefreshSignal> asReconnectSignalConsumer() {
        return refreshSignal -> {
            System.out.println("ToolWindowGui::asReconnectSignalConsumer");
            matchesComboBox.removeAllItems();
        };
    }

    @NotNull
    public Consumer<RefreshSignal> asRecompileSignalConsumer() {
        return refreshSignal -> {
            System.out.println("ToolWindowGui::asRecompileSignalConsumer");
            sourceRemoteMatched = null;
            showError("");
        };
    }

    @NotNull
    public Consumer<ProjectSettings> asProjectSettingsConsumer() {
        return projectSettings -> {
            System.out.println("ToolWindowGui::asProjectSettingsConsumer");
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
        };
    }

    @NotNull
    public Consumer<SourceRemoteMatched> asSourceRemoteMatchedConsumer() {
        return sourceRemoteMatched_ -> {
            System.out.println("ToolWindowGui::asSourceRemoteMatchedConsumer");
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
        };
    }

    @NotNull
    public Consumer<CompiledText> asCompiledTextConsumer() {
        return compiledText -> {
            System.out.println("ToolWindowGui::asCompiledTextConsumer");
            suppressUpdates = true;
            editor.setNewDocumentAndFileType(AsmFileType.INSTANCE, editor.getDocument());
            editor.setText(compiledText.getCompiledText());
            editor.setEnabled(true);
            suppressUpdates = false;
        };
    }

    @NotNull
    public Consumer<Error> asErrorConsumer() {
        return error -> {
            System.out.println("ToolWindowGui::asErrorConsumer");
            suppressUpdates = true;
            editor.setNewDocumentAndFileType(PlainTextFileType.INSTANCE, editor.getDocument());
            editor.setText(filterOutTerminalEscapeSequences(error.getMessage()));
            editor.setEnabled(false);
            suppressUpdates = false;
        };
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

