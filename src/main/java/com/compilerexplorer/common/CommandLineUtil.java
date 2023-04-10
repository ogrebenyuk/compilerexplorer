package com.compilerexplorer.common;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class CommandLineUtil {
    @NotNull
    public static List<String> parseCommandLine(@NotNull String command) {
        List<String> result = new ArrayList<>();
        StringBuilder currentOption = null;
        boolean isInsideDoubleQuotes = false;
        for (char c : (command + ' ').toCharArray()) {
            if (c == ' ' && !isInsideDoubleQuotes) {
                if (currentOption != null) {
                    result.add(currentOption.toString());
                    currentOption = null;
                }
            } else {
                if (c == '"') {
                    isInsideDoubleQuotes = !isInsideDoubleQuotes;
                } else {
                    if (currentOption == null) {
                        currentOption = new StringBuilder();
                    }
                    currentOption.append(c);
                }
            }
        }
        return result;
    }

    @NotNull
    public static String formCommandLine(@NotNull List<String> options) {
        return options.stream()
                .map(opt -> opt.contains(" ") || opt.isEmpty() ? '"' + opt + '"' : opt)
                .collect(Collectors.joining(" "));
    }
}
