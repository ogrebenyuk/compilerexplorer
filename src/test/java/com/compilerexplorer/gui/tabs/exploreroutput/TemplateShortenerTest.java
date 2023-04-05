package com.compilerexplorer.gui.tabs.exploreroutput;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

@NonNls
class TemplateShortenerTest {
    @Test
    void verifyNoTemplatesNotShortened() {
        verifyNotShortened("");
        verifyNotShortened(" ");
        verifyNotShortened("a");
    }

    @Test
    void verifyOperatorsNotShortened() {
        verifyNotShortened("operator<");
        verifyNotShortened("operator< ");
        verifyNotShortened("operator<(");
        verifyNotShortened("operator< (");
        verifyNotShortened("operator<=");
        verifyNotShortened("operator<= ");
        verifyNotShortened("operator<=(");
        verifyNotShortened("operator<= (");
        verifyNotShortened("operator>");
        verifyNotShortened("operator> ");
        verifyNotShortened("operator>(");
        verifyNotShortened("operator> (");
        verifyNotShortened("operator>=");
        verifyNotShortened("operator>= ");
        verifyNotShortened("operator>=(");
        verifyNotShortened("operator>= (");
        verifyNotShortened("operator<<");
        verifyNotShortened("operator<< ");
        verifyNotShortened("operator<<(");
        verifyNotShortened("operator<< (");
        verifyNotShortened("operator<<=");
        verifyNotShortened("operator<<= ");
        verifyNotShortened("operator<<=(");
        verifyNotShortened("operator<<= (");
        verifyNotShortened("operator>>");
        verifyNotShortened("operator>> ");
        verifyNotShortened("operator>>(");
        verifyNotShortened("operator>> (");
        verifyNotShortened("operator>>=");
        verifyNotShortened("operator>>= ");
        verifyNotShortened("operator>>=(");
        verifyNotShortened("operator>>= (");
        verifyNotShortened("operator<=>");
        verifyNotShortened("operator<=> ");
        verifyNotShortened("operator<=>(");
        verifyNotShortened("operator<=> (");
        verifyNotShortened("operator->");
        verifyNotShortened("operator-> ");
        verifyNotShortened("operator->(");
        verifyNotShortened("operator-> (");
    }

    @Test
    void verifySimpleTemplatesShortened() {
        verifyShortened("<x>", "<...>");
        verifyShortened(" <x>",  " <...>");
        verifyShortened("<x> ",  "<...> ");
        verifyShortened(" <x> ",  " <...> ");
        verifyShortened("a<x>", "a<...>");
        verifyShortened("a<x>::f<y>", "a<...>::f<...>");
    }

    @Test
    void verifyNestedTemplatesShortened() {
        verifyShortened("a<x<y>>", "a<...>");
        verifyShortened("a<x<y>>::f<z<t<u>>>", "a<...>::f<...>");
    }

    @Test
    void verifyTemplateOperatorsShortened() {
        verifyShortened("operator< <x>", "operator< <...>");
        verifyShortened("operator<< <x>", "operator<< <...>");
        verifyShortened("operator> <x>", "operator> <...>");
        verifyShortened("operator>> <x>", "operator>> <...>");
    }

    @Test
    void verifyMalformedAddressesNotRecognized() {
        verifyShortened(" <a+0x0>", " <...>");
        verifyShortened("10 <+0x10>", "10 <...>");
        verifyShortened("abc <a0xabc>", "abc <...>");
        verifyShortened("0 <a-x0>", "0 <...>");
        verifyShortened("10 <a-010>", "10 <...>");
        verifyShortened("abc <a-0x>", "abc <...>");
        verifyShortened("abc <a-0xabc", "abc <...");
    }

    @Test
    void verifyAddressesWithoutTemplatesNotShortened() {
        verifyNotShortened("0 <a+0x0>");
        verifyNotShortened("10 <a+0x10>");
        verifyNotShortened("abc <a+0xabc>");
        verifyNotShortened("0 <a-0x0>");
        verifyNotShortened("10 <a-0x10>");
        verifyNotShortened("abc <a-0xabc>");
    }

    @Test
    void verifyAddressesWithOperatorsNotShortened() {
        verifyNotShortened("8 <operator<+0x8>");
    }

    @Test
    void verifyAddressesWithTemplateOperatorsShortened() {
        verifyShortened("8 <operator< <x>+0x8>", "8 <operator< <...>+0x8>");
    }

    @Test
    void verifyAddressesWithTemplatesShortened() {
        verifyShortened("1f <a<x>+0x1f>", "1f <a<...>+0x1f>");
        verifyShortened("1f <a<xy zt>+0x1f>", "1f <a<...>+0x1f>");
        verifyShortened("1f <a<x<y>>+0x1f>", "1f <a<...>+0x1f>");
        verifyShortened("1f <a<xy zt<qwerty>>+0x1f>", "1f <a<...>+0x1f>");
    }
    @Test
    void verifyTemplatesNextToAddressesShortened() {
        verifyShortened("g<x>1f <a+0x1f>", "g<...>1f <a+0x1f>");
        verifyShortened("g<x> 1f <a+0x1f>", "g<...> 1f <a+0x1f>");
        verifyShortened("1f <a+0x1f>g<x>", "1f <a+0x1f>g<...>");
        verifyShortened("1f <a+0x1f> g<x>", "1f <a+0x1f> g<...>");
    }

    @Test
    void verifyMultipleAddressesAndTemplates() {
        verifyShortened("g<x>1f <a+0x1f>h<x>1f <a<x>+0x1f>j<x>", "g<...>1f <a+0x1f>h<...>1f <a<...>+0x1f>j<...>");
    }

    @Test
    void verifyRelaxedAddressesNotShortened() {
        verifyNotShortened("111 <a+0x0>");
        verifyNotShortened("111 <a-0x0>");
    }

    @Test
    void verifyExceptionsNotShortened() {
        verifyExceptionNotShortened("<built-in>");
        verifyExceptionNotShortened("<command-line>");
        verifyExceptionNotShortened("<__static_initialization_and_destruction_0(int, int)>");
        verifyExceptionNotShortened("<_GLOBAL__sub_I_main>");
    }

    @Test
    void verifyAddressesWithKnownSuffixesNotShortened() {
        verifyNotShortened("0 <printf@plt>");
    }

    @Test
    void verifyTemplatesInAddressesWithKnownSuffixesShortened() {
        verifyShortened("0 <f<g>@plt>", "0 <f<...>@plt>");
    }

    private void verifyExceptionNotShortened(@NotNull String exception) {
        verifyNotShortened(exception);
        verifyNotShortened(" " + exception);
        verifyNotShortened("abc" + exception);
        verifyNotShortened(exception + " ");
        verifyNotShortened(exception + "abc");
    }

    private void verifyShortened(@NotNull String fullText, @NotNull String expectedShortenedText) {
        StringBuilder builder = new StringBuilder();
        TemplateShortener.shortenTemplates(builder, fullText);
        Assertions.assertEquals(expectedShortenedText, builder.toString());
    }

    private void verifyNotShortened(@NotNull String textWithoutTemplates) {
        verifyShortened(textWithoutTemplates, textWithoutTemplates);
    }
}