package com.compilerexplorer.datamodel;

import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.cidr.system.HostMachine;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

public class SourceSettings implements Visitable {
    @NotNull
    public final VirtualFile source;
    @NotNull
    public final String sourcePath;
    @NotNull
    public final String sourceName;
    @NotNull
    public final String language;
    @NotNull
    public final String languageSwitch;
    @NotNull
    public final File compiler;
    @NotNull
    public final String compilerPath;
    @NotNull
    public final File compilerWorkingDir;
    @NotNull
    public final String compilerKind;
    @NotNull
    public final List<String> switches;
    @NotNull
    public final HostMachine host;

    public SourceSettings(@NotNull VirtualFile source_, @NotNull String sourcePath_, @NotNull String language_, @NotNull String languageSwitch_, @NotNull File compiler_, @NotNull String compilerKind_, @NotNull List<String> switches_, @NotNull HostMachine host_) {
        source = source_;
        sourcePath = sourcePath_;
        sourceName = source_.getPresentableName();
        language = language_;
        languageSwitch = languageSwitch_;
        compiler = compiler_;
        compilerPath = compiler_.getPath();
        compilerWorkingDir = compiler_.getParentFile();
        compilerKind = compilerKind_;
        switches = switches_;
        host = host_;
    }

    @Override
    public void accept(@NotNull Visitor visitor) {
        visitor.visit(this);
    }

    @Override
    public int hashCode() {
        return source.hashCode()
                + language.hashCode()
                + languageSwitch.hashCode()
                + FileUtil.fileHashCode(compiler)
                + compilerKind.hashCode()
                + switches.hashCode()
                + host.hashCode()
                ;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof SourceSettings other)) {
            return false;
        }
        return source.equals(other.source)
                && language.equals(other.language)
                && languageSwitch.equals(other.languageSwitch)
                && FileUtil.filesEqual(compiler, other.compiler)
                && compilerKind.equals(other.compilerKind)
                && switches.equals(other.switches)
                && host.equals(other.host)
                ;
    }
}
