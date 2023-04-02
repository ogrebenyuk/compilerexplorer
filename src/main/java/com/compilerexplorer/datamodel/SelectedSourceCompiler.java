package com.compilerexplorer.datamodel;

import com.compilerexplorer.datamodel.state.LocalCompilerSettings;
import com.intellij.openapi.util.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

public class SelectedSourceCompiler {
    public static final Key<SelectedSourceCompiler> KEY = Key.create(SelectedSourceCompiler.class.getName());

    private final boolean cached;
    private final boolean canceled;
    private boolean isSupportedCompilerType;
    @Nullable
    private final CompilerResult result;
    @Nullable
    private final LocalCompilerSettings localCompilerSettings;

    public SelectedSourceCompiler(boolean cached_, boolean canceled_, boolean isSupportedCompilerType_, @Nullable CompilerResult result_, @Nullable LocalCompilerSettings localCompilerSettings_) {
        cached = cached_;
        canceled = canceled_;
        isSupportedCompilerType = isSupportedCompilerType_;
        result = result_;
        localCompilerSettings = localCompilerSettings_;
    }

    public boolean getCached() {
        return cached;
    }

    public boolean getCanceled() {
        return canceled;
    }

    public boolean getIsSupportedCompilerType() {
        return isSupportedCompilerType;
    }

    @NotNull
    public Optional<CompilerResult> getResult() {
        return Optional.ofNullable(result);
    }

    @NotNull
    public Optional<LocalCompilerSettings> getLocalCompilerSettings() {
        return Optional.ofNullable(localCompilerSettings);
    }

    @Override
    public int hashCode() {
        return (cached ? 1 : 0) + (canceled ? 1 : 0) + (isSupportedCompilerType ? 1 : 0) + Objects.hashCode(result) + Objects.hashCode(localCompilerSettings);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof SelectedSourceCompiler other)) {
            return false;
        }
        return cached == other.cached && canceled == other.canceled && isSupportedCompilerType == other.isSupportedCompilerType && Objects.equals(result, other.result) && Objects.equals(localCompilerSettings, other.localCompilerSettings);
    }
}
