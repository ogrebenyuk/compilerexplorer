package com.compilerexplorer.common.datamodel.state;

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
    @Property
    @SerializedName("optOutput")
    private boolean optOutput = false;

    public Filters() {
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

    public boolean getOptOutput() {
        return optOutput;
    }

    public void setOptOutput(boolean optOutput_) {
        optOutput = optOutput_;
    }

    public void copyFrom(@NotNull Filters other) {
        setBinary(other.getBinary());
        setExecute(other.getExecute());
        setLabels(other.getLabels());
        setDirectives(other.getDirectives());
        setCommentOnly(other.getCommentOnly());
        setTrim(other.getTrim());
        setIntel(other.getIntel());
        setDemangle(other.getDemangle());
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
        return getBinary() == other.getBinary()
                && getExecute() == other.getExecute()
                && getLabels() == other.getLabels()
                && getDirectives() == other.getDirectives()
                && getCommentOnly() == other.getCommentOnly()
                && getTrim() == other.getTrim()
                && getIntel() == other.getIntel()
                && getDemangle() == other.getDemangle()
                && getOptOutput() == other.getOptOutput()
                ;
    }
}
