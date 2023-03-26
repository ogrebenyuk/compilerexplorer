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
    @Nullable
    private SourceRemoteMatched sourceRemoteMatched;
    @Nullable
    private Consumer<SourceRemoteMatched> sourceRemoteMatchedConsumer;

    public MatchesGui(@NotNull SuppressionFlag suppressionFlag_) {
        suppressionFlag = suppressionFlag_;

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
        comboBox.addItemListener(new ComboBoxSelectionListener<>(comboBox, new LaterConsumerUnlessSuppressed<>(this::selectCompilerMatch, suppressionFlag)));
    }

    @NotNull
    public Component getComponent() {
        return comboBox;
    }

    public void setSourceRemoteMatchedConsumer(@NotNull Consumer<SourceRemoteMatched> sourceRemoteMatchedConsumer_) {
        sourceRemoteMatchedConsumer = sourceRemoteMatchedConsumer_;
    }

    @NotNull
    public Consumer<RefreshSignal> asReconnectSignalConsumer() {
        return this::reconnect;
    }

    @Override
    public void accept(@NotNull SourceRemoteMatched sourceRemoteMatched_) {
        suppressionFlag.apply(() -> sourceRemoteMatchedChanged(sourceRemoteMatched_));
    }

    public void clearSourceRemoteMatched() {
        sourceRemoteMatched = null;
    }

    private void sourceRemoteMatchedChanged(@NotNull SourceRemoteMatched sourceRemoteMatched_) {
        ApplicationManager.getApplication().assertIsDispatchThread();
        sourceRemoteMatched = sourceRemoteMatched_;
        CompilerMatch chosenMatch = sourceRemoteMatched.getRemoteCompilerMatches().getChosenMatch();
        List<CompilerMatch> matches = sourceRemoteMatched.getRemoteCompilerMatches().getOtherMatches();
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
        selectCompilerMatch(newSelection);
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

    private void selectCompilerMatch(@Nullable CompilerMatch compilerMatch) {
        if (compilerMatch != null) {
            comboBox.setToolTipText(getMatchTooltip(compilerMatch));
        } else {
            clear();
        }
        if (sourceRemoteMatchedConsumer != null && sourceRemoteMatched != null) {
            sourceRemoteMatchedConsumer.accept(compilerMatch != null ? sourceRemoteMatched.withChosenMatch(compilerMatch) : null);
        }
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
