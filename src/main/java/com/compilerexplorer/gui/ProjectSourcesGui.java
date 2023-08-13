package com.compilerexplorer.gui;

import com.compilerexplorer.common.*;
import com.compilerexplorer.common.component.BaseComponent;
import com.compilerexplorer.common.component.CEComponent;
import com.compilerexplorer.common.component.DataHolder;
import com.compilerexplorer.common.component.ResetFlag;
import com.compilerexplorer.datamodel.ProjectSources;
import com.compilerexplorer.datamodel.SelectedSource;
import com.compilerexplorer.datamodel.SourceSettings;
import com.compilerexplorer.gui.listeners.ComboBoxSelectionListener;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.SimpleListCellRenderer;
import org.jetbrains.annotations.Nls;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ItemListener;
import java.util.Objects;

public class ProjectSourcesGui extends BaseComponent {
    @NonNls
    private static final Logger LOG = Logger.getInstance(ProjectSourcesGui.class);

    @NotNull
    private final ComboBox<SourceSettings> comboBox;
    @NotNull
    private final SuppressionFlag suppressionFlag;
    @Nullable private ItemListener selectionListener;

    public ProjectSourcesGui(@NotNull CEComponent nextComponent, @NotNull SuppressionFlag suppressionFlag_) {
        super(nextComponent);
        LOG.debug("created");

        suppressionFlag = suppressionFlag_;

        comboBox = new ComboBox<>();
        comboBox.setRenderer(new SimpleListCellRenderer<>() {
            @Override
            public void customize(@Nullable JList list, @Nullable SourceSettings value, int index, boolean isSelected, boolean cellHasFocus) {
                setText(value != null ? getText(value) : "");
                setIcon(value != null ? LanguageUtil.getLanguageIcon(value.language) : null);
            }
            @NotNull
            private String getText(@NotNull SourceSettings value) {
                return value.sourceName;
            }
        });
    }

    @Override
    protected void doClear(@NotNull DataHolder data) {
        LOG.debug("doClear");
        data.remove(SelectedSource.KEY);
    }

    @Override
    protected void doReset() {
        LOG.debug("doReset");
        ApplicationManager.getApplication().assertIsDispatchThread();
        comboBox.removeAllItems();
        comboBox.setToolTipText(null);
        comboBox.setEnabled(false);
    }

    @Override
    protected void doRefresh(@NotNull DataHolder data) {
        LOG.debug("doRefresh");
        data.get(ProjectSources.KEY).ifPresentOrElse(
                projectSettings -> sourcesChanged(data, projectSettings),
                () -> {
                    LOG.debug("cannot find input");
                    doReset();
                }
        );
    }

    @NotNull
    public Component getComponent() {
        return comboBox;
    }

    private void sourcesChanged(@NotNull DataHolder data, @NotNull ProjectSources projectSources) {
        ApplicationManager.getApplication().assertIsDispatchThread();

        if (selectionListener != null) {
            comboBox.removeItemListener(selectionListener);
        }
        selectionListener = new ComboBoxSelectionListener<>(comboBox,
                new LaterConsumerUnlessSuppressed<>(selectedSource -> selectionChanged(ResetFlag.without(data), selectedSource, true), suppressionFlag)
        );
        comboBox.addItemListener(selectionListener);

        SourceSettings oldSelection = comboBox.getItemAt(comboBox.getSelectedIndex());
        SourceSettings newSelection = projectSources.getSources().stream()
                .filter(x -> oldSelection != null && Objects.equals(x.sourcePath, oldSelection.sourcePath))
                .findFirst()
                .orElse(!projectSources.getSources().isEmpty() ? projectSources.getSources().get(0) : null);
        DefaultComboBoxModel<SourceSettings> model = new DefaultComboBoxModel<>(projectSources.getSources().toArray(new SourceSettings[0]));
        model.setSelectedItem(newSelection);
        LOG.debug("showing " + model.getSize() + " sources");
        comboBox.setModel(model);
        selectionChanged(data, newSelection, false);
        comboBox.setEnabled(model.getSize() > 0);
    }

    private void selectionChanged(@NotNull DataHolder data, @Nullable SourceSettings newSelection, boolean needRefreshNext) {
        LOG.debug("selectionChanged to " + (newSelection != null ? newSelection.sourcePath : null) + ", will refresh next: " + needRefreshNext);
        if (newSelection != null) {
            data.put(SelectedSource.KEY, new SelectedSource(newSelection));
            comboBox.setToolTipText(getSourceTooltip(newSelection));
        } else {
            doClear(data);
            doReset();
        }
        if (needRefreshNext) {
            refreshNext(data);
        }
    }

    @Nls
    @NotNull
    private String getSourceTooltip(@NotNull SourceSettings sourceSettings) {
        return TooltipUtil.prettify(Bundle.format("compilerexplorer.ProjectSourcesGui.Tooltip",
                "Source", sourceSettings.sourcePath,
                "Language", sourceSettings.language,
                "CompilerPath", sourceSettings.compilerPath,
                "CompilerKind", sourceSettings.compilerKind,
                "CompilerOptions", CommandLineUtil.formCommandLine(sourceSettings.switches)));
    }
}
