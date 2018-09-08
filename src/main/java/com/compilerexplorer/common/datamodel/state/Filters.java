package com.compilerexplorer.common.datamodel.state;

import com.google.gson.annotations.SerializedName;
import com.intellij.util.xmlb.annotations.Property;
import org.jetbrains.annotations.NotNull;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.PROPERTY)
public class Filters {
    @Property
    @SerializedName("intel")
    private boolean intel = true;
    @Property
    @SerializedName("commentOnly")
    private boolean commentOnly = true;
    @Property
    @SerializedName("directives")
    private boolean directives = true;
    @Property
    @SerializedName("labels")
    private boolean labels = true;
    @Property
    @SerializedName("optOutput")
    private boolean optOutput = false;

    public Filters() {
        // empty
    }

    public Filters(@NotNull Filters other) {
        copyFrom(other);
    }

    public boolean getIntel() {
        return intel;
    }

    public void setIntel(boolean intel_) {
        intel = intel_;
    }

    public boolean getCommentOnly() {
        return commentOnly;
    }

    public void setCommentOnly(boolean commentOnly_) {
        commentOnly = commentOnly_;
    }

    public boolean getDirectives() {
        return directives;
    }

    public void setDirectives(boolean directives_) {
        directives = directives_;
    }

    public boolean getLabels() {
        return labels;
    }

    public void setLabels(boolean labels_) {
        labels = labels_;
    }

    public boolean getOptOutput() {
        return optOutput;
    }

    public void setOptOutput(boolean optOutput_) {
        optOutput = optOutput_;
    }

    public void copyFrom(@NotNull Filters other) {
        setIntel(other.getIntel());
        setCommentOnly(other.getCommentOnly());
        setDirectives(other.getDirectives());
        setLabels(other.getLabels());
        setOptOutput(other.getOptOutput());
    }

    @Override
    public int hashCode() {
        return 0;
    }

    @Override
    public boolean equals(@NotNull Object obj) {
        if (!(obj instanceof Filters)) {
            return false;
        }
        Filters other = (Filters)obj;
        return getIntel() == other.getIntel()
            && getCommentOnly() == other.getCommentOnly()
            && getDirectives() == other.getDirectives()
            && getLabels() == other.getLabels()
            && getOptOutput() == other.getOptOutput()
        ;
    }
}
