package com.compilerexplorer.common;

import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.jetbrains.cidr.lang.OCLanguageKind;
import com.jetbrains.cidr.lang.workspace.compiler.OCCompilerKind;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

public class SourceSettings {
    @NotNull
    private final VirtualFile source;
    @NotNull
    private final OCLanguageKind language;
    @NotNull
    private final File compiler;
    @NotNull
    private final OCCompilerKind compilerKind;
    @NotNull
    private final List<String> switches;

    public SourceSettings(@NotNull VirtualFile source_, @NotNull OCLanguageKind language_, @NotNull File compiler_, @NotNull OCCompilerKind compilerKind_, @NotNull List<String> switches_) {
        source = source_;
        language = language_;
        compiler = compiler_;
        compilerKind = compilerKind_;
        switches = switches_;
    }

    @NotNull
    public VirtualFile getSource() {
        return source;
    }

    @NotNull
    public OCLanguageKind getLanguage() {
        return language;
    }

    @NotNull
    public File getCompiler() {
        return compiler;
    }

    @NotNull
    public OCCompilerKind getCompilerKind() {
        return compilerKind;
    }

    @NotNull
    public List<String> getSwitches() {
        return switches;
    }

    @Override
    public int hashCode() {
        return source.hashCode()
               + language.hashCode()
               + FileUtil.fileHashCode(compiler)
               + compilerKind.hashCode()
               + switches.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof  SourceSettings)) {
            return false;
        }
        SourceSettings rhs = (SourceSettings)obj;
        return source.equals(rhs.source)
               && language.equals(rhs.language)
               && FileUtil.filesEqual(compiler, rhs.compiler)
               && compilerKind.equals(rhs.compilerKind)
               && String.join(" ", switches).equals(String.join(" ", rhs.switches));
    }
}
