package com.compilerexplorer.datamodel;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;

public class SourceSettingsConnected {
    @NotNull
    public final SelectedSourceSettings sourceSettings;

    @Nullable
    public String remoteCompilersEndpoint;
    public boolean remoteCompilersQueried;
    @Nullable
    public String remoteCompilersRawOutput;
    @Nullable
    public Exception remoteCompilersException;

    public SourceSettingsConnected(@NotNull SelectedSourceSettings sourceSettings_) {
        sourceSettings = sourceSettings_;
    }

    public boolean isValid() {
        return remoteCompilersException == null;
    }

    @Override
    public int hashCode() {
        return sourceSettings.hashCode()
                + (remoteCompilersEndpoint != null ? remoteCompilersEndpoint.hashCode() : 0)
                + (remoteCompilersQueried ? 1 : 0)
                + (remoteCompilersRawOutput != null ? remoteCompilersRawOutput.hashCode() : 0)
                + (remoteCompilersException != null ? remoteCompilersException.hashCode() : 0)
                ;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof SourceSettingsConnected other)) {
            return false;
        }
        return sourceSettings.equals(other.sourceSettings)
                && Objects.equals(remoteCompilersEndpoint, other.remoteCompilersEndpoint)
                && remoteCompilersQueried == other.remoteCompilersQueried
                && Objects.equals(remoteCompilersRawOutput, other.remoteCompilersRawOutput)
                && Objects.equals(remoteCompilersException, other.remoteCompilersException)
                ;
    }
}
