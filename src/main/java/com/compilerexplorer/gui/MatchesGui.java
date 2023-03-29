package com.compilerexplorer.gui;

import com.compilerexplorer.common.LaterConsumerUnlessSuppressed;
import com.compilerexplorer.common.RefreshSignal;
import com.compilerexplorer.common.SuppressionFlag;
import com.compilerexplorer.datamodel.SourceRemoteMatched;
import com.compilerexplorer.datamodel.state.CompilerMatch;
import com.compilerexplorer.datamodel.state.CompilerMatchKind;
import com.compilerexplorer.gui.listeners.ComboBoxSelectionListener;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.SimpleListCellRenderer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemListener;
import java.util.List;
import java.util.Objects;
import java.util.Vector;
import java.util.function.Consumer;
import java.util.stream.Collectors;

public class MatchesGui implements Consumer<SourceRemoteMatched> {
    @NotNull
    private final ComboBox<CompilerMatch> comboBox;
    @NotNull
    private final SuppressionFlag suppressionFlag;
    @NotNull
    private final Consumer<SourceRemoteMatched> sourceRemoteMatchedConsumer;
    @Nullable
    private ItemListener selectionListener;

    public MatchesGui(@NotNull SuppressionFlag suppressionFlag_, @NotNull Consumer<SourceRemoteMatched> sourceRemoteMatchedConsumer_) {
        suppressionFlag = suppressionFlag_;
        sourceRemoteMatchedConsumer = sourceRemoteMatchedConsumer_;

        comboBox = new ComboBox<>();
        comboBox.setRenderer(new SimpleListCellRenderer<>() {
            @Override
            public void customize(@Nullable JList list, @Nullable CompilerMatch value, int index, boolean isSelected, boolean cellHasFocus) {
                setText((value != null) ? getText(value) : "");
            }
            @NotNull
            private String getText(@NotNull CompilerMatch value) {
                return value.getRemoteCompilerInfo().getName() + (value.getCompilerMatchKind() != CompilerMatchKind.NO_MATCH ? " (" + CompilerMatchKind.asString(value.getCompilerMatchKind()) + ")" : "");
            }
        });
    }

    @NotNull
    public Component getComponent() {
        return comboBox;
    }

    @NotNull
    public Consumer<RefreshSignal> asReconnectSignalConsumer() {
        return this::reconnect;
    }

    @Override
    public void accept(@NotNull SourceRemoteMatched sourceRemoteMatched) {
        suppressionFlag.apply(() -> sourceRemoteMatchedChanged(sourceRemoteMatched));
    }

    private void sourceRemoteMatchedChanged(@NotNull SourceRemoteMatched sourceRemoteMatched) {
        ApplicationManager.getApplication().assertIsDispatchThread();

        if (selectionListener != null) {
            comboBox.removeItemListener(selectionListener);
        }
        selectionListener = new ComboBoxSelectionListener<>(comboBox,
                new LaterConsumerUnlessSuppressed<>(selectedMatch -> selectCompilerMatch(sourceRemoteMatched, selectedMatch), suppressionFlag)
        );
        comboBox.addItemListener(selectionListener);

        if (sourceRemoteMatched.isValid()) {
            assert sourceRemoteMatched.remoteCompilerMatches != null;

            CompilerMatch chosenMatch = sourceRemoteMatched.remoteCompilerMatches.getChosenMatch();
            List<CompilerMatch> matches = sourceRemoteMatched.remoteCompilerMatches.getOtherMatches();
            CompilerMatch oldSelection = comboBox.getItemAt(comboBox.getSelectedIndex());
            CompilerMatch newSelection = matches.stream()
                    .filter(x -> oldSelection != null && x.getRemoteCompilerInfo().getId().equals(oldSelection.getRemoteCompilerInfo().getId()))
                    .findFirst()
                    .orElse(!chosenMatch.getRemoteCompilerInfo().getId().isEmpty() ? chosenMatch : (matches.size() != 0 ? matches.get(0) : null));
            DefaultComboBoxModel<CompilerMatch> model = new DefaultComboBoxModel<>(
                    matches.stream()
                            .map(x -> newSelection == null || !newSelection.getRemoteCompilerInfo().getId().equals(x.getRemoteCompilerInfo().getId()) ? x : newSelection)
                            .filter(Objects::nonNull).collect(Collectors.toCollection(Vector::new)));
            model.setSelectedItem(newSelection);
            comboBox.setModel(model);
            selectCompilerMatch(sourceRemoteMatched, newSelection);
        } else {
            selectCompilerMatch(sourceRemoteMatched, null);
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

    private void selectCompilerMatch(@NotNull SourceRemoteMatched sourceRemoteMatched, @Nullable CompilerMatch compilerMatch) {
        if (compilerMatch != null) {
            comboBox.setToolTipText(getMatchTooltip(compilerMatch));
        } else {
            clear();
        }
        sourceRemoteMatchedConsumer.accept(sourceRemoteMatched.withChosenMatch(compilerMatch != null ? compilerMatch : new CompilerMatch()));
    }

    private void reconnect(RefreshSignal refreshSignal) {
        ApplicationManager.getApplication().assertIsDispatchThread();
        clear();
    }

    private void clear() {
        comboBox.removeAllItems();
        comboBox.setToolTipText("");
    }
}
