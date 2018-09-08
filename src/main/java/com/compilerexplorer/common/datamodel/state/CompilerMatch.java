package com.compilerexplorer.common.datamodel.state;

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

    public CompilerMatch(@NotNull CompilerMatch other) {
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

    public void setRemoteCompilerInfo(@NotNull RemoteCompilerInfo remoteCompilerInfo_) {
        remoteCompilerInfo = new RemoteCompilerInfo(remoteCompilerInfo_);
    }

    @NotNull
    public CompilerMatchKind getCompilerMatchKind() {
        return compilerMatchKind;
    }

    void setCompilerMatchKind(@NotNull CompilerMatchKind compilerMatchKind_) {
        compilerMatchKind = compilerMatchKind_;
    }

    public void copyFrom(@NotNull CompilerMatch other) {
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
    public boolean equals(@NotNull Object obj) {
        if (!(obj instanceof CompilerMatch)) {
            return false;
        }
        CompilerMatch other = (CompilerMatch)obj;
        return getRemoteCompilerInfo().equals(other.getRemoteCompilerInfo())
                && getCompilerMatchKind().equals(other.getCompilerMatchKind())
                ;
    }

}
