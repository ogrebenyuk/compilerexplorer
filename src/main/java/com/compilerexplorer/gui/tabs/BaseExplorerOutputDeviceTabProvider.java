package com.compilerexplorer.gui.tabs;

import com.compilerexplorer.common.Bundle;
import com.compilerexplorer.common.Tabs;
import com.compilerexplorer.common.component.DataHolder;
import com.compilerexplorer.datamodel.CompiledText;
import com.compilerexplorer.datamodel.state.SettingsState;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Optional;

public class BaseExplorerOutputDeviceTabProvider extends BaseExplorerOutputTabProvider {
    private final int device;

    public BaseExplorerOutputDeviceTabProvider(@NotNull SettingsState state, @NotNull Tabs tab, @NonNls @NotNull String actionId, int device_) {
        super(state, tab, actionId, compilerResult -> getDeviceAsm(compilerResult, device_));
        device = device_;
    }

    @Nullable
    private static CompiledText.AsmResult getDeviceAsm(@NotNull CompiledText.CompiledResult compiledResult, int device_) {
        return findDeviceEntry(compiledResult, device_).map(Map.Entry::getValue).orElse(null);
    }

    @NonNls
    @Nullable
    public String getDeviceName(@NotNull CompiledText.CompiledResult compiledResult) {
        return findDeviceEntry(compiledResult, device).map(Map.Entry::getKey).orElse(null);
    }

    @Override
    public void provide(@NotNull DataHolder data, @NotNull TabContentConsumer contentConsumer) {
        shouldHaveRun(data).ifPresentOrElse(unusedPreprocessedText -> compiledText(data).ifPresent(compiledText -> compiledText.getCompiledResultIfGood().map(compilerResult -> getDeviceAsm(compilerResult, device)).ifPresentOrElse(
                unusedAsm -> super.provide(data, contentConsumer),
                () -> message(false, () -> {clear(); return Bundle.get("compilerexplorer.BaseExplorerOutputDeviceTabProvider.NoDevice");}, contentConsumer)
            )),
            () -> message(false, () -> {clear(); return Bundle.get("compilerexplorer.ExplorerOutputTabProvider.WasNotRun");}, contentConsumer)
        );
    }

    @NotNull
    private static Optional<Map.Entry<String, CompiledText.AsmResult>> findDeviceEntry(@NotNull CompiledText.CompiledResult compiledResult, int device_) {
        if (compiledResult.devices != null) {
            return compiledResult.devices.entrySet().stream().skip(device_ - 1).findFirst();
        } else {
            return Optional.empty();
        }
    }
}
