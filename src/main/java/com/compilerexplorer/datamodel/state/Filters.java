package com.compilerexplorer.datamodel.state;

import com.google.gson.annotations.SerializedName;
import com.intellij.util.xmlb.annotations.Property;
import org.jetbrains.annotations.NotNull;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.PROPERTY)
public class Filters {
    @Property
    @SerializedName("binary")
    private boolean binary = false;
    @Property
    @SerializedName("execute")
    private boolean execute = false;
    @Property
    @SerializedName("labels")
    private boolean labels = true;
    @Property
    @SerializedName("directives")
    private boolean directives = true;
    @Property
    @SerializedName("commentOnly")
    private boolean commentOnly = true;
    @Property
    @SerializedName("trim")
    private boolean trim = false;
    @Property
    @SerializedName("intel")
    private boolean intel = true;
    @Property
    @SerializedName("demangle")
    private boolean demangle = false;

    Filters() {
        // empty
    }

    public Filters(@NotNull Filters other) {
        copyFrom(other);
    }

    public boolean getBinary() {
        return binary;
    }

    public void setBinary(boolean binary_) {
        binary = binary_;
    }

    public boolean getExecute() {
        return execute;
    }

    public void setExecute(boolean execute_) {
        execute = execute_;
    }

    public boolean getLabels() {
        return labels;
    }

    public void setLabels(boolean labels_) {
        labels = labels_;
    }

    public boolean getDirectives() {
        return directives;
    }

    public void setDirectives(boolean directives_) {
        directives = directives_;
    }

    public boolean getCommentOnly() {
        return commentOnly;
    }

    public void setCommentOnly(boolean commentOnly_) {
        commentOnly = commentOnly_;
    }

    public boolean getTrim() {
        return trim;
    }

    public void setTrim(boolean trim_) {
        trim = trim_;
    }

    public boolean getIntel() {
        return intel;
    }

    public void setIntel(boolean intel_) {
        intel = intel_;
    }

    public boolean getDemangle() {
        return demangle;
    }

    public void setDemangle(boolean demangle_) {
        demangle = demangle_;
    }

    private void copyFrom(@NotNull Filters other) {
        setBinary(other.getBinary());
        setExecute(other.getExecute());
        setLabels(other.getLabels());
        setDirectives(other.getDirectives());
        setCommentOnly(other.getCommentOnly());
        setTrim(other.getTrim());
        setIntel(other.getIntel());
        setDemangle(other.getDemangle());
    }

    @Override
    public int hashCode() {
        return (getBinary() ? 1 : 0)
                + (getExecute() ? 1 : 0)
                + (getLabels() ? 1 : 0)
                + (getDirectives() ? 1 : 0)
                + (getCommentOnly() ? 1 : 0)
                + (getTrim() ? 1 : 0)
                + (getIntel() ? 1 : 0)
                + (getDemangle() ? 1 : 0)
                ;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Filters other)) {
            return false;
        }
        return getBinary() == other.getBinary()
                && getExecute() == other.getExecute()
                && getLabels() == other.getLabels()
                && getDirectives() == other.getDirectives()
                && getCommentOnly() == other.getCommentOnly()
                && getTrim() == other.getTrim()
                && getIntel() == other.getIntel()
                && getDemangle() == other.getDemangle()
                ;
    }
}
