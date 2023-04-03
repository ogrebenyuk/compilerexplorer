package com.compilerexplorer.datamodel;

import com.jetbrains.cidr.system.HostMachine;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class SourceSettings {
    @NotNull
    public final String sourcePath;
    @NotNull
    public final String sourceName;
    @NotNull
    public final String language;
    @NotNull
    public final String languageSwitch;
    @NotNull
    public final String compilerPath;
    @NotNull
    public final String compilerWorkingDir;
    @NotNull
    public final String compilerKind;
    @NotNull
    public final List<String> switches;
    @NotNull
    public final HostMachine host;

    public SourceSettings(@NotNull String sourcePath_,
                          @NotNull String sourceName_,
                          @NotNull String language_,
                          @NotNull String languageSwitch_,
                          @NotNull String compilerPath_,
                          @NotNull String compilerWorkingDir_,
                          @NotNull String compilerKind_,
                          @NotNull List<String> switches_,
                          @NotNull HostMachine host_) {
        sourcePath = sourcePath_;
        sourceName = sourceName_;
        language = language_;
        languageSwitch = languageSwitch_;
        compilerPath = compilerPath_;
        compilerWorkingDir = compilerWorkingDir_;
        compilerKind = compilerKind_;
        switches = switches_;
        host = host_;
    }

    @Override
    public int hashCode() {
        return sourcePath.hashCode()
                + sourceName.hashCode()
                + language.hashCode()
                + languageSwitch.hashCode()
                + compilerPath.hashCode()
                + compilerWorkingDir.hashCode()
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
        return sourcePath.equals(other.sourcePath)
                && sourceName.equals(other.sourceName)
                && language.equals(other.language)
                && languageSwitch.equals(other.languageSwitch)
                && compilerPath.equals(other.compilerPath)
                && compilerWorkingDir.equals(other.compilerWorkingDir)
                && compilerKind.equals(other.compilerKind)
                && switches.equals(other.switches)
                && host.equals(other.host)
                ;
    }
}
