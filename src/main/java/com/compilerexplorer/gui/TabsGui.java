package com.compilerexplorer.gui;

import com.compilerexplorer.common.ActionUtil;
import com.compilerexplorer.common.Constants;
import com.intellij.openapi.actionSystem.*;
import com.intellij.util.ui.JBUI;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;

public class TabsGui {
    @NotNull
    private final JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
    @NotNull
    private final DefaultActionGroup tabsGroup = (DefaultActionGroup) ActionUtil.findAction("compilerexplorer.TabsPopupGroup");
    @NotNull
    private final ActionToolbar toolbar;
    @Nullable
    private AnAction selectedAction = null;

    public TabsGui() {
        JLabel label = new JLabel();
        toolbar = ActionManager.getInstance().createActionToolbar(ActionPlaces.TOOLBAR, new DefaultActionGroup(new DefaultCompactActionGroup() {
            @Override
            public void update(@NotNull final AnActionEvent event) {
                super.update(event);
                Presentation presentation = event.getPresentation();
                String text = null;
                String description = null;
                Icon icon = null;
                if (selectedAction != null) {
                    Presentation source = ActionUtil.createPresentation(selectedAction, panel);
                    text = source.getText();
                    description = source.getDescription();
                    icon = source.getIcon();
                    if (icon == Constants.EMPTY_ICON) {
                        icon = null;
                    }
                }
                presentation.setPopupGroup(true);
                presentation.setText(text);
                presentation.setDescription(description);
                label.setIcon(icon);
            }
            @Override
            public AnAction @NotNull [] getChildren(@Nullable AnActionEvent e) {
                return tabsGroup.getChildren(e);
            }
            @Override
            public @NotNull ActionUpdateThread getActionUpdateThread() {
                return ActionUpdateThread.EDT;
            }
            @Override
            public boolean displayTextInToolbar() {
                return true;
            }
        }), true);
        toolbar.setReservePlaceAutoPopupIcon(false);
        toolbar.setTargetComponent(panel);
        ((JPanel) toolbar).setBorder(JBUI.Borders.empty());
        ((JPanel) toolbar).setOpaque(false);
        panel.setBorder(JBUI.Borders.empty());
        panel.setOpaque(false);
        panel.add(label);
        panel.add(toolbar.getComponent());
    }

    @NotNull
    public Component getComponent() {
        return panel;
    }

    public void selectAction(@NotNull AnAction action) {
        selectedAction = action;
        toolbar.updateActionsImmediately();
    }
}
