package com.compilerexplorer.settings.gui;

import com.compilerexplorer.common.Constants;
import com.intellij.openapi.ui.ComboBox;
import com.intellij.ui.SimpleListCellRenderer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.Component;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class UrlGui {
    private static class UrlHistoryRenderer extends SimpleListCellRenderer<String> {
        @Override
        public void customize(@Nullable JList list, @Nullable String value, int index, boolean isSelected, boolean cellHasFocus) {
            setText((value != null) ? getText(value) : "");
        }

        @NotNull
        private String getText(@NotNull String value) {
            @Nullable String description = Constants.DEFAULT_URLS.get(value);
            return description != null && !description.isEmpty() ? description + ": " + value : value;
        }
    }

    @NotNull
    private final ComboBox<String> urlField = new ComboBox<>();

    public UrlGui() {
        urlField.setEditable(true);
        urlField.setRenderer(new UrlHistoryRenderer());
    }

    @NotNull
    public Component getComponent() {
        return urlField;
    }

    @NotNull
    public String getUrl() {
        String url = (String) urlField.getSelectedItem();
        assert url != null;
        return url;
    }

    public void setUrl(@NotNull String url) {
        urlField.setSelectedItem(url);
    }

    @NotNull
    public List<String> getUrlHistory() {
        List<String> items = new ArrayList<>();
        for (int i = Constants.DEFAULT_URLS.size(); i < urlField.getModel().getSize(); ++i) {
            items.add(urlField.getModel().getElementAt(i));
        }
        return items;
    }

    public void setUrlHistory(@NotNull List<String> urlHistory) {
        DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>(
            Stream.concat(
                Constants.DEFAULT_URLS.keySet().stream(),
                urlHistory.stream()
            ).collect(Collectors.toCollection(Vector::new)));
        model.setSelectedItem(urlField.getModel() != null ? urlField.getModel().getSelectedItem() : "");
        urlField.setModel(model);
    }

    public void showUrlHistory() {
        urlField.showPopup();
    }
}
