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
    @NotNull
    private final String defines;

    public SourceSettings(@NotNull VirtualFile source_, @NotNull OCLanguageKind language_, @NotNull File compiler_, @NotNull OCCompilerKind compilerKind_, @NotNull List<String> switches_, @NotNull String defines_) {
        source = source_;
        language = language_;
        compiler = compiler_;
        compilerKind = compilerKind_;
        switches = switches_;
        defines = defines_;
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

    @NotNull
    public String getDefines() {
        return defines;
    }

    @Override
    public int hashCode() {
        return source.hashCode()
               + language.hashCode()
               + FileUtil.fileHashCode(compiler)
               + compilerKind.hashCode()
                + switches.hashCode()
                + defines.hashCode()
        ;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof  SourceSettings)) {
            return false;
        }
        SourceSettings other = (SourceSettings)obj;
        return source.equals(other.source)
               && language.equals(other.language)
               && FileUtil.filesEqual(compiler, other.compiler)
               && compilerKind.equals(other.compilerKind)
               && String.join(" ", switches).equals(String.join(" ", other.switches))
               && defines.equals(other.defines)
        ;
    }
}
