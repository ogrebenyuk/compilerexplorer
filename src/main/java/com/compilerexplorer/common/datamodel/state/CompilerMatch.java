package com.compilerexplorer.common.datamodel.state;

import com.intellij.util.xmlb.annotations.Property;
import org.jetbrains.annotations.NotNull;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.PROPERTY)
public class CompilerMatch {
    @NotNull
    @Property
    private RemoteCompilerId remoteCompilerId = new RemoteCompilerId();
    @NotNull
    @Property
    private CompilerMatchKind compilerMatchKind = CompilerMatchKind.NO_MATCH;

    public CompilerMatch() {
        // empty
    }

    public CompilerMatch(@NotNull CompilerMatch other) {
        copyFrom(other);
    }

    public CompilerMatch(@NotNull RemoteCompilerId remoteCompilerId_, @NotNull CompilerMatchKind matchKind_) {
        setRemoteCompilerId(remoteCompilerId_);
        setCompilerMatchKind(matchKind_);
    }

    @NotNull
    public RemoteCompilerId getRemoteCompilerId() {
        return remoteCompilerId;
    }

    public void setRemoteCompilerId(@NotNull RemoteCompilerId remoteCompilerId_) {
        remoteCompilerId = new RemoteCompilerId(remoteCompilerId_);
    }

    @NotNull
    public CompilerMatchKind getCompilerMatchKind() {
        return compilerMatchKind;
    }

    void setCompilerMatchKind(@NotNull CompilerMatchKind compilerMatchKind_) {
        compilerMatchKind = compilerMatchKind_;
    }

    public void copyFrom(@NotNull CompilerMatch other) {
        setRemoteCompilerId(other.getRemoteCompilerId());
        setCompilerMatchKind(other.getCompilerMatchKind());
    }

    @Override
    public int hashCode() {
        return getRemoteCompilerId().hashCode()
                + getCompilerMatchKind().hashCode()
                ;
    }

    @Override
    public boolean equals(@NotNull Object obj) {
        if (!(obj instanceof CompilerMatch)) {
            return false;
        }
        CompilerMatch other = (CompilerMatch)obj;
        return getRemoteCompilerId().equals(other.getRemoteCompilerId())
                && getCompilerMatchKind().equals(other.getCompilerMatchKind())
                ;
    }

}
