package com.compilerexplorer.datamodel.state;

import com.google.gson.annotations.SerializedName;
import com.intellij.util.xmlb.annotations.Property;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

@XmlAccessorType(XmlAccessType.PROPERTY)
public class Filters {
    @NonNls
    @NotNull
    private static final String BINARY_FIELD = "binary";
    @NonNls
    @NotNull
    private static final String BINARY_OBJECT_FIELD = "binaryObject";
    @NonNls
    @NotNull
    private static final String EXECUTE_FIELD = "execute";
    @NonNls
    @NotNull
    private static final String LABELS_FIELD = "labels";
    @NonNls
    @NotNull
    private static final String LIBRARY_CODE_FIELD = "libraryCode";
    @NonNls
    @NotNull
    private static final String DIRECTIVES_FIELD = "directives";
    @NonNls
    @NotNull
    private static final String COMMENT_ONLY_FIELD = "commentOnly";
    @NonNls
    @NotNull
    private static final String TRIM_FIELD = "trim";
    @NonNls
    @NotNull
    private static final String INTEL_FIELD = "intel";
    @NonNls
    @NotNull
    private static final String DEMANGLE_FIELD = "demangle";

    @Property
    @SerializedName(BINARY_FIELD)
    private boolean binary = false;
    @Property
    @SerializedName(BINARY_OBJECT_FIELD)
    private boolean binaryObject = false;
    @Property
    @SerializedName(EXECUTE_FIELD)
    private boolean execute = false;
    @Property
    @SerializedName(LABELS_FIELD)
    private boolean labels = true;
    @Property
    @SerializedName(LIBRARY_CODE_FIELD)
    private boolean libraryCode = false;
    @Property
    @SerializedName(DIRECTIVES_FIELD)
    private boolean directives = true;
    @Property
    @SerializedName(COMMENT_ONLY_FIELD)
    private boolean commentOnly = true;
    @Property
    @SerializedName(TRIM_FIELD)
    private boolean trim = false;
    @Property
    @SerializedName(INTEL_FIELD)
    private boolean intel = true;
    @Property
    @SerializedName(DEMANGLE_FIELD)
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

    public boolean getBinaryObject() {
        return binaryObject;
    }

    public void setBinaryObject(boolean binaryObject_) {
        binaryObject = binaryObject_;
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

    public boolean getLibraryCode() {
        return libraryCode;
    }

    public void setLibraryCode(boolean libraryCode_) {
        libraryCode = libraryCode_;
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
        setBinaryObject(other.getBinaryObject());
        setExecute(other.getExecute());
        setLabels(other.getLabels());
        setLibraryCode(other.getLibraryCode());
        setDirectives(other.getDirectives());
        setCommentOnly(other.getCommentOnly());
        setTrim(other.getTrim());
        setIntel(other.getIntel());
        setDemangle(other.getDemangle());
    }

    @Override
    public int hashCode() {
        return (getBinary() ? 1 : 0)
                + (getBinaryObject() ? 1 : 0)
                + (getExecute() ? 1 : 0)
                + (getLabels() ? 1 : 0)
                + (getLibraryCode() ? 1 : 0)
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
                && getBinaryObject() == other.getBinaryObject()
                && getExecute() == other.getExecute()
                && getLabels() == other.getLabels()
                && getLibraryCode() == other.getLibraryCode()
                && getDirectives() == other.getDirectives()
                && getCommentOnly() == other.getCommentOnly()
                && getTrim() == other.getTrim()
                && getIntel() == other.getIntel()
                && getDemangle() == other.getDemangle()
                ;
    }
}
