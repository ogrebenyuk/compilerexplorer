package com.compilerexplorer.datamodel;

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
    private final String sourcePath;
    @NotNull
    private final String sourceName;
    @NotNull
    private final OCLanguageKind language;
    @NotNull
    private final String languageSwitch;
    @NotNull
    private final File compiler;
    @NotNull
    private final OCCompilerKind compilerKind;
    @NotNull
    private final List<String> switches;

    public SourceSettings(@NotNull VirtualFile source_, @NotNull String sourcePath_, @NotNull OCLanguageKind language_, @NotNull String languageSwitch_, @NotNull File compiler_, @NotNull OCCompilerKind compilerKind_, @NotNull List<String> switches_) {
        source = source_;
        sourcePath = sourcePath_;
        sourceName = source.getPresentableName();
        language = language_;
        languageSwitch = languageSwitch_;
        compiler = compiler_;
        compilerKind = compilerKind_;
        switches = switches_;
    }

    @NotNull
    public VirtualFile getSource() {
        return source;
    }

    @NotNull
    public String getSourcePath() {
        return sourcePath;
    }

    @NotNull
    public String getSourceName() {
        return sourceName;
    }

    @NotNull
    public OCLanguageKind getLanguage() {
        return language;
    }

    @NotNull
    public String getLanguageSwitch() {
        return languageSwitch;
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
        return getSource().hashCode()
                + getLanguage().hashCode()
                + getLanguageSwitch().hashCode()
                + FileUtil.fileHashCode(getCompiler())
                + getCompilerKind().hashCode()
                + getSwitches().hashCode()
                ;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof  SourceSettings)) {
            return false;
        }
        SourceSettings other = (SourceSettings)obj;
        return getSource().getPath().equals(other.getSource().getPath())
                && getLanguage().getDisplayName().equals(other.getLanguage().getDisplayName())
                && getLanguageSwitch().equals(other.getLanguageSwitch())
                && FileUtil.filesEqual(getCompiler(), other.getCompiler())
                && getCompilerKind().toString().equals(other.getCompilerKind().toString())
                && String.join(" ", getSwitches()).equals(String.join(" ", other.getSwitches()))
                ;
    }
}
