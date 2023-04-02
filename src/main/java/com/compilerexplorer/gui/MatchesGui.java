package com.compilerexplorer.gui;

import com.compilerexplorer.common.LaterConsumerUnlessSuppressed;
import com.compilerexplorer.common.SuppressionFlag;
import com.compilerexplorer.common.component.BaseComponent;
import com.compilerexplorer.common.component.CEComponent;
import com.compilerexplorer.common.component.DataHolder;
import com.compilerexplorer.common.component.ResetFlag;
import com.compilerexplorer.datamodel.SourceRemoteMatched;
import com.compilerexplorer.datamodel.state.CompilerMatch;
import com.compilerexplorer.datamodel.state.CompilerMatchKind;
import com.compilerexplorer.gui.listeners.ComboBoxSelectionListener;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
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
import java.util.stream.Collectors;

public class MatchesGui extends BaseComponent {
    private static final Logger LOG = Logger.getInstance(MatchesGui.class);

    @NotNull
    private final ComboBox<CompilerMatch> comboBox;
    @NotNull
    private final SuppressionFlag suppressionFlag;
    @Nullable
    private ItemListener selectionListener;

    public MatchesGui(@NotNull CEComponent nextComponent, @NotNull SuppressionFlag suppressionFlag_) {
        super(nextComponent);
        LOG.debug("created");

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
    }

    @NotNull
    public Component getComponent() {
        return comboBox;
    }

    @Override
    protected void doClear(@NotNull DataHolder data) {
        LOG.debug("doClear");
        data.remove(SourceRemoteMatched.SELECTED_KEY);
    }

    @Override
    protected void doReset(@NotNull DataHolder data) {
        LOG.debug("doReset");
        ApplicationManager.getApplication().assertIsDispatchThread();
        comboBox.removeAllItems();
        comboBox.setToolTipText(null);
        comboBox.setEnabled(false);
    }

    protected void doRefresh(@NotNull DataHolder data) {
        LOG.debug("doRefresh");
        suppressionFlag.apply(() -> data.get(SourceRemoteMatched.KEY).ifPresentOrElse(
                sourceRemoteMatched -> showMatches(data, sourceRemoteMatched),
                () -> {
                    LOG.debug("cannot find input");
                    doReset(data);
                }
        ));
    }

    private void showMatches(@NotNull DataHolder data, @NotNull SourceRemoteMatched sourceRemoteMatched) {
        ApplicationManager.getApplication().assertIsDispatchThread();

        if (selectionListener != null) {
            comboBox.removeItemListener(selectionListener);
        }
        selectionListener = new ComboBoxSelectionListener<>(comboBox,
                new LaterConsumerUnlessSuppressed<>(selectedMatch -> selectCompilerMatch(ResetFlag.without(data), sourceRemoteMatched, selectedMatch, true), suppressionFlag)
        );
        comboBox.addItemListener(selectionListener);

        CompilerMatch chosenMatch = sourceRemoteMatched.getMatches().getChosenMatch();
        List<CompilerMatch> matches = sourceRemoteMatched.getMatches().getOtherMatches();
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
        LOG.debug("showing " + model.getSize() + " remote compilers");
        comboBox.setModel(model);
        selectCompilerMatch(data, sourceRemoteMatched, newSelection, false);
        comboBox.setEnabled(model.getSize() > 0);
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

    private void selectCompilerMatch(@NotNull DataHolder data, @NotNull SourceRemoteMatched sourceRemoteMatched, @Nullable CompilerMatch compilerMatch, boolean needRefreshNext) {
        LOG.debug("selectionChanged to " + (compilerMatch != null ? compilerMatch.getRemoteCompilerInfo().getName() : null) + ", will refresh next: " + needRefreshNext);
        if (compilerMatch != null) {
            data.put(SourceRemoteMatched.SELECTED_KEY, sourceRemoteMatched.withChosenMatch(compilerMatch));
            comboBox.setToolTipText(getMatchTooltip(compilerMatch));
        } else {
            doClear(data);
            doReset(data);
        }
        if (needRefreshNext) {
            refreshNext(data);
        }
    }
}
