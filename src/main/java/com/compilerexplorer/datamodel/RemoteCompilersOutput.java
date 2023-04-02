package com.compilerexplorer.datamodel;

import com.intellij.openapi.util.Key;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.Optional;

public class RemoteCompilersOutput {
    public static final Key<RemoteCompilersOutput> KEY = Key.create(RemoteCompilersOutput.class.getName());

    public static class Output {
        @Nullable
        private final String rawOutput;
        @Nullable
        private final Exception exception;

        public Output(@Nullable String rawOutput_, @Nullable Exception exception_) {
            rawOutput = rawOutput_;
            exception = exception_;
        }

        @NotNull
        public Optional<String> getRawOutput() {
            return Optional.ofNullable(rawOutput);
        }

        @NotNull
        public Optional<Exception> getException() {
            return Optional.ofNullable(exception);
        }

        @Override
        public int hashCode() {
            return Objects.hashCode(rawOutput) + Objects.hashCode(exception);
        }

        @Override
        public boolean equals(Object obj) {
            if (!(obj instanceof Output other)) {
                return false;
            }
            return Objects.equals(rawOutput, other.rawOutput) && Objects.equals(exception, other.exception);
        }
    }

    @NotNull
    private final String endpoint;
    private final boolean cached;
    private final boolean canceled;
    @Nullable
    private final Output output;

    public RemoteCompilersOutput(@NotNull String endpoint_, boolean cached_, boolean canceled_, @Nullable Output output_) {
        endpoint = endpoint_;
        cached = cached_;
        canceled = canceled_;
        output = output_;
    }

    @NotNull
    public String getEndpoint() {
        return endpoint;
    }

    public boolean getCached() {
        return cached;
    }

    public boolean getCanceled() {
        return canceled;
    }

    @NotNull
    public Optional<Output> getOutput() {
        return Optional.ofNullable(output);
    }

    @Override
    public int hashCode() {
        return endpoint.hashCode() + (cached ? 1 : 0) + Objects.hashCode(output);
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof RemoteCompilersOutput other)) {
            return false;
        }
        return endpoint.equals(other.endpoint) && cached == other.cached && Objects.equals(output, other.output);
    }
}
