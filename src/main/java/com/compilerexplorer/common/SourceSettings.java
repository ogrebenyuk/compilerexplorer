package com.compilerexplorer.common;

import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

public class SourceSettings {
    @NotNull
    private final VirtualFile source;
    @NotNull
    private final File compiler;
    @NotNull
    private final List<String> switches;

    public SourceSettings(@NotNull VirtualFile source_, @NotNull File compiler_, @NotNull List<String> switches_) {
        source = source_;
        compiler = compiler_;
        switches = switches_;
    }

    @NotNull
    public VirtualFile getSource() {
        return source;
    }

    @NotNull
    public File getCompiler() {
        return compiler;
    }

    @NotNull
    public List<String> getSwitches() {
        return switches;
    }

    @Override
    public int hashCode() {
        return source.hashCode() + FileUtil.fileHashCode(compiler) + switches.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof  SourceSettings)) {
            return false;
        }
        SourceSettings rhs = (SourceSettings)obj;
        return source.equals(rhs.source) && FileUtil.filesEqual(compiler, rhs.compiler) && String.join(" ", switches).equals(String.join(" ", rhs.switches));
    }
}
