package com.compilerexplorer.gui.tabs;

import com.compilerexplorer.common.Bundle;
import com.compilerexplorer.common.component.DataHolder;
import com.compilerexplorer.datamodel.state.SettingsState;
import org.jetbrains.annotations.*;
import org.junit.jupiter.api.Assertions;
import org.mockito.ArgumentCaptor;
import org.mockito.exceptions.base.MockitoException;

import java.util.*;

import static org.mockito.Mockito.*;

public class BaseTabsTest {
    @NotNull
    private final ResourceBundle bundle = Bundle.adopt(mock(ResourceBundle.class));

    @NotNull
    private List<CollectedTabContent> run(@NotNull DataHolder data, @NotNull SettingsState state) {
        List<CollectedTabContent> result = new ArrayList<>();
        TabsFactory.create(state).forEach(provider -> provider.provide(data, (enabled, error, filetype, defaultExtension, contentProducer) -> {
            ArgumentCaptor<String> keyCaptor = ArgumentCaptor.forClass(String.class);
            when(bundle.getString(keyCaptor.capture())).thenReturn("");
            TabContent content = contentProducer.produce();
            String key = null;
            try {
                key = keyCaptor.getValue();
            } catch (MockitoException e) {
                // empty
            }
            result.add(new CollectedTabContent(provider.getTab(), enabled, error, filetype, defaultExtension, content.getFolding().isPresent(), key));
        }));
        return result;
    }

    protected void verify(@NotNull List<CollectedTabContent> expectedResult, @NotNull DataHolder data, @NotNull SettingsState state) {
        @NotNull List<CollectedTabContent> actualResult = run(data, state);
        Assertions.assertEquals(expectedResult, actualResult);
    }
}
