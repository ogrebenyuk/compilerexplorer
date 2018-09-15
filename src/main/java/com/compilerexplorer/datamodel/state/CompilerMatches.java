package com.compilerexplorer.datamodel.state;

import com.intellij.util.xmlb.annotations.Property;
import org.jetbrains.annotations.NotNull;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@XmlAccessorType(XmlAccessType.PROPERTY)
public class CompilerMatches {
    @NotNull
    @Property
    private CompilerMatch chosenMatch = new CompilerMatch();
    @NotNull
    @Property
    private List<CompilerMatch> otherMatches = new ArrayList<>();

    public CompilerMatches() {
        // empty
    }

    @SuppressWarnings("CopyConstructorMissesField")
    public CompilerMatches(@NotNull CompilerMatches other) {
        copyFrom(other);
    }

    public CompilerMatches(@NotNull CompilerMatch chosenMatch_, @NotNull List<CompilerMatch> otherMatches_) {
        setChosenMatch(chosenMatch_);
        setOtherMatches(otherMatches_);
    }

    @NotNull
    public CompilerMatch getChosenMatch() {
        return chosenMatch;
    }

    private void setChosenMatch(@NotNull CompilerMatch chosenMatch_) {
        chosenMatch = new CompilerMatch(chosenMatch_);
    }

    @NotNull
    public List<CompilerMatch> getOtherMatches() {
        return otherMatches;
    }

    private void setOtherMatches(@NotNull List<CompilerMatch> otherMatches_) {
        otherMatches = otherMatches_.stream().map(CompilerMatch::new).collect(Collectors.toList());
    }

    private void copyFrom(@NotNull CompilerMatches other) {
        setChosenMatch(other.getChosenMatch());
        setOtherMatches(other.getOtherMatches());
    }

    @Override
    public int hashCode() {
        return getChosenMatch().hashCode()
                + getOtherMatches().hashCode()
                ;
    }

    @Override
    public boolean equals(@NotNull Object obj) {
        if (!(obj instanceof CompilerMatches)) {
            return false;
        }
        CompilerMatches other = (CompilerMatches)obj;
        return getChosenMatch().equals(other.getChosenMatch())
                && getOtherMatches().equals(other.getOtherMatches())
                ;
    }

}
