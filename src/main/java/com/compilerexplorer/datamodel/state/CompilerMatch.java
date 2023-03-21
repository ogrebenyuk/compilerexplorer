package com.compilerexplorer.datamodel.state;

import com.intellij.util.xmlb.annotations.Property;
import org.jetbrains.annotations.NotNull;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.PROPERTY)
public class CompilerMatch {
    @NotNull
    @Property
    private RemoteCompilerInfo remoteCompilerInfo = new RemoteCompilerInfo();
    @NotNull
    @Property
    private CompilerMatchKind compilerMatchKind = CompilerMatchKind.NO_MATCH;

    public CompilerMatch() {
        // empty
    }

    @SuppressWarnings("CopyConstructorMissesField")
    CompilerMatch(@NotNull CompilerMatch other) {
        copyFrom(other);
    }

    public CompilerMatch(@NotNull RemoteCompilerInfo remoteCompilerInfo_, @NotNull CompilerMatchKind matchKind_) {
        setRemoteCompilerInfo(remoteCompilerInfo_);
        setCompilerMatchKind(matchKind_);
    }

    @NotNull
    public RemoteCompilerInfo getRemoteCompilerInfo() {
        return remoteCompilerInfo;
    }

    private void setRemoteCompilerInfo(@NotNull RemoteCompilerInfo remoteCompilerInfo_) {
        remoteCompilerInfo = new RemoteCompilerInfo(remoteCompilerInfo_);
    }

    @NotNull
    public CompilerMatchKind getCompilerMatchKind() {
        return compilerMatchKind;
    }

    private void setCompilerMatchKind(@NotNull CompilerMatchKind compilerMatchKind_) {
        compilerMatchKind = compilerMatchKind_;
    }

    private void copyFrom(@NotNull CompilerMatch other) {
        setRemoteCompilerInfo(other.getRemoteCompilerInfo());
        setCompilerMatchKind(other.getCompilerMatchKind());
    }

    @Override
    public int hashCode() {
        return getRemoteCompilerInfo().hashCode()
                + getCompilerMatchKind().hashCode()
                ;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof CompilerMatch other)) {
            return false;
        }
        return getRemoteCompilerInfo().equals(other.getRemoteCompilerInfo())
                && getCompilerMatchKind().equals(other.getCompilerMatchKind())
                ;
    }

}
