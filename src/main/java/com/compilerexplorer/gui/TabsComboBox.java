package com.compilerexplorer.gui;

import com.compilerexplorer.common.*;
import com.compilerexplorer.datamodel.state.SettingsState;
import com.compilerexplorer.gui.listeners.ComboBoxSelectionListener;
import com.intellij.openapi.actionSystem.*;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.PopupMenuListenerAdapter;
import com.intellij.ui.SimpleListCellRenderer;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.plaf.basic.BasicComboPopup;
import java.awt.*;
import java.util.List;
import java.util.Vector;

public class TabsComboBox extends ComboBox<AnAction> {
    @NotNull
    private final SuppressionFlag suppressUpdates = new SuppressionFlag();

    @NotNull
    private ComboBox<AnAction> combobox() {
        return this;
    }

    public TabsComboBox(@NotNull SettingsState state) {
        combobox().setMaximumRowCount(Tabs.values().length + 1);
        combobox().setBorder(JBUI.Borders.empty());
        combobox().setRenderer(new SimpleListCellRenderer<>() {
            @Override
            public void customize(@Nullable JList list, @Nullable AnAction value, int index, boolean isSelected, boolean cellHasFocus) {
                if (value != null) {
                    Presentation presentation = createPresentation(value);
                    @NonNls String text = presentation.getText();
                    setText(text);
                    setToolTipText(presentation.getDescription() != null ? TooltipUtil.prettify(presentation.getDescription()) : null);

                    if (index == combobox().getModel().getSize() - 1) {
                        setIcon(state.getShowAllTabs() ? Constants.CHECKMARK_ICON : Constants.EMPTY_ICON);
                    } else if (index == -1 && presentation.getIcon() == Constants.EMPTY_ICON) {
                        setIcon(null);
                    } else {
                        setIcon(presentation.getIcon());
                    }
                }
                setBorder(JBUI.Borders.empty());
            }
        });
        combobox().addItemListener(new ComboBoxSelectionListener<>(combobox(), action -> suppressUpdates.unlessApplied(() ->
            executeAction(action)
        )));
        combobox().addPopupMenuListener(new PopupMenuListenerAdapter() {
            @Override
            public void popupMenuWillBecomeVisible(@NotNull PopupMenuEvent event) {
                if (combobox().getItemCount() == 0) return;
                final Object child = combobox().getAccessibleContext().getAccessibleChild(0);
                if (child instanceof BasicComboPopup popup) {
                    SwingUtilities.invokeLater(() -> customizePopup(popup));
                }
            }

            private void customizePopup(@NotNull BasicComboPopup popup) {
                @NotNull JScrollPane scrollPane = getScrollPane(popup);
                popupWider(popup, scrollPane);
                Point location = combobox().getLocationOnScreen();
                int height = combobox().getPreferredSize().height;
                popup.setLocation(location.x, location.y + height - 1);
                popup.setLocation(location.x, location.y + height);
            }

            private void popupWider(BasicComboPopup popup, @NotNull JScrollPane scrollPane) {
                //  Determine the maximum width to use:
                //  a) determine the popup preferred width
                //  b) limit width to the maximum if specified
                //  c) ensure width is not less than the scroll pane width

                int popupWidth = popup.getList().getPreferredSize().width
                        + 5  // make sure horizontal scrollbar doesn't appear
                        + getScrollBarWidth(scrollPane);

                Dimension scrollPaneSize = scrollPane.getPreferredSize();
                popupWidth = Math.max(popupWidth, scrollPaneSize.width);

                //  Adjust the width

                scrollPaneSize.width = popupWidth;
                scrollPane.setPreferredSize(scrollPaneSize);
                scrollPane.setMaximumSize(scrollPaneSize);
            }

            private int getScrollBarWidth(@NotNull JScrollPane scrollPane) {
                if (combobox().getItemCount() > combobox().getMaximumRowCount()) {
                    return scrollPane.getVerticalScrollBar().getPreferredSize().width;
                }
                return 0;
            }

            @NotNull
            private JScrollPane getScrollPane(@NotNull BasicComboPopup popup) {
                return (JScrollPane) SwingUtilities.getAncestorOfClass(JScrollPane.class, popup.getList());
            }
        });
    }

    @NotNull
    public Component getComponent() {
        return this;
    }

    public void refreshModel(@NotNull List<AnAction> actions, @Nullable AnAction selectedAction) {
        Vector<AnAction> vec = new Vector<>(actions);
        vec.add(ActionUtil.findAction("compilerexplorer.ShowAllTabsToggle"));
        DefaultComboBoxModel<AnAction> model = new DefaultComboBoxModel<>(vec);
        suppressUpdates.apply(() -> combobox().setModel(model));
        if (selectedAction != null) {
            selectAction(selectedAction, true);
        }
    }

    public void selectAction(@NotNull AnAction action, boolean suppressUpdates_) {
        suppressUpdates.apply(() -> combobox().setSelectedItem(action));
        Presentation presentation = createPresentation(action);
        combobox().setToolTipText(presentation.getDescription());
        refreshUI();
        if (!suppressUpdates_) {
            executeAction(action);
        }
    }

    public void applyThemeColors() {
        refreshUI();
    }

    private void refreshUI() {
        combobox().setPrototypeDisplayValue((AnAction) combobox().getSelectedItem());
        combobox().updateUI();
        combobox().setBorder(JBUI.Borders.empty());
    }

    private void executeAction(@NotNull AnAction action) {
        action.actionPerformed(createEvent(action));
    }

    @NotNull
    private AnActionEvent createEvent(@NotNull AnAction action) {
        return ActionUtil.createEvent(action, combobox());
    }

    @NotNull
    private Presentation createPresentation(@NotNull AnAction action) {
        return ActionUtil.createPresentation(action, combobox());
    }
}
