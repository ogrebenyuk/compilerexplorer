package com.compilerexplorer.gui;

import com.compilerexplorer.common.datamodel.*;
import com.compilerexplorer.common.datamodel.state.CompilerMatch;
import com.compilerexplorer.common.datamodel.state.CompilerMatchKind;
import com.compilerexplorer.common.datamodel.state.CompilerMatches;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.EditorFactory;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.fileTypes.PlainTextFileType;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.ListCellRendererWrapper;
import com.intellij.ui.components.panels.HorizontalLayout;
import org.jetbrains.annotations.NotNull;
import com.jetbrains.cidr.lang.asm.AsmFileType;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemEvent;
import java.util.List;
import java.util.Objects;
import java.util.Vector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class ToolWindowGui implements ProjectSettingsConsumer, CompiledTextConsumer, SourceRemoteMatchedConsumer {
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
    private boolean suppressUpdates = false;

    public ToolWindowGui(@NotNull Project project) {
        content = new JPanel(new BorderLayout());
        JPanel headPanel = new JPanel(new HorizontalLayout(0));
        projectSettingsComboBox = new ComboBox<>();
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
        headPanel.add(projectSettingsComboBox, HorizontalLayout.LEFT);
        matchesComboBox = new ComboBox<>();
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
        headPanel.add(matchesComboBox, HorizontalLayout.LEFT);
        content.add(headPanel, BorderLayout.NORTH);
        JPanel mainPanel = new JPanel(new BorderLayout());
        content.add(mainPanel, BorderLayout.CENTER);
        editor = new EditorTextField(EditorFactory.getInstance().createDocument(""), project, PlainTextFileType.INSTANCE, true, false) {
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
        List<CompilerMatch> matches = sourceRemoteMatched.getRemoteCompilerMatches().getOtherMatches();
        CompilerMatch oldSelection = matchesComboBox.getItemAt(matchesComboBox.getSelectedIndex());
        CompilerMatch newSelection = matches.stream()
                .filter(x -> oldSelection != null && x.getRemoteCompilerInfo().getId().equals(oldSelection.getRemoteCompilerInfo().getId()))
                .findFirst()
                .orElse(matches.size() != 0 ? matches.get(0) : null);
        DefaultComboBoxModel<CompilerMatch> model = new DefaultComboBoxModel<>(
                Stream.concat(
                        Stream.of(newSelection),
                        matches.stream().filter(x -> newSelection == null || !newSelection.getRemoteCompilerInfo().getId().equals(x.getRemoteCompilerInfo().getId()))
                ).filter(Objects::nonNull).collect(Collectors.toCollection(Vector::new)));
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
        editor.setNewDocumentAndFileType(AsmFileType.INSTANCE, editor.getDocument());
        editor.setText(compiledText.getCompiledText());
        editor.setEnabled(true);
    }

    @Override
    public void clearCompiledText(@NotNull String reason) {
        showError(reason);
    }

    private void showError(@NotNull String reason) {
        editor.setNewDocumentAndFileType(PlainTextFileType.INSTANCE, editor.getDocument());
        editor.setText(filterOutTerminalEscapeSequences(reason));
        editor.setEnabled(false);
    }

    @NotNull
    private static String filterOutTerminalEscapeSequences(@NotNull String terminalText) {
        return terminalText.replaceAll("\u001B\\[[;\\d]*.", "");
    }
}

