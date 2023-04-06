package com.compilerexplorer.gui.tabs;

import com.compilerexplorer.common.Bundle;
import com.compilerexplorer.common.Tabs;
import com.intellij.json.JsonFileType;
import com.intellij.openapi.fileTypes.FileType;
import com.intellij.openapi.fileTypes.PlainTextFileType;
import com.jetbrains.cidr.lang.OCFileType;
import com.jetbrains.cidr.lang.asm.AsmFileType;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.annotations.PropertyKey;

public interface Base {
    boolean ENABLED = true;
    boolean DISABLED = false;
    boolean ERROR = true;
    boolean NO_ERROR = false;
    boolean WITH_FOLDING = true;
    boolean NO_FOLDING = false;
    @NotNull
    FileType TEXT = PlainTextFileType.INSTANCE;
    @NotNull
    FileType JSON = JsonFileType.INSTANCE;
    @NotNull
    FileType SOURCE = OCFileType.INSTANCE;
    @NotNull
    FileType ASM = AsmFileType.INSTANCE;

    @NotNull
    private static CollectedTabContent collect(@NotNull Tabs tab,
                                               boolean enabled,
                                               boolean error,
                                               @NotNull FileType filetype,
                                               @NonNls @NotNull String defaultExtension,
                                               boolean folding,
                                               @NonNls @Nullable @PropertyKey(resourceBundle = Bundle.BUNDLE_FILE) String key) {
        return new CollectedTabContent(tab, enabled, error, filetype, defaultExtension, folding, key);
    }

    @NotNull
    default CollectedTabContent error(@NotNull Tabs tab,
                                      boolean enabled,
                                      @NonNls @Nullable @PropertyKey(resourceBundle = Bundle.BUNDLE_FILE) String key) {
        return collect(tab, enabled, ERROR, TEXT, TEXT.getDefaultExtension(), NO_FOLDING, key);
    }

    @NotNull
    default CollectedTabContent message(@NotNull Tabs tab,
                                        boolean enabled,
                                        @NonNls @Nullable @PropertyKey(resourceBundle = Bundle.BUNDLE_FILE) String key) {
        return collect(tab, enabled, NO_ERROR, TEXT, TEXT.getDefaultExtension(), NO_FOLDING, key);
    }

    @NotNull
    default CollectedTabContent content(@NotNull Tabs tab,
                                        boolean enabled,
                                        @NotNull FileType filetype,
                                        @NonNls @NotNull String defaultExtension) {
        return collect(tab, enabled, NO_ERROR, filetype, defaultExtension, NO_FOLDING, null);
    }

    @NotNull
    default CollectedTabContent content(@NotNull Tabs tab,
                                        boolean enabled,
                                        @NotNull FileType filetype) {
        return content(tab, enabled, filetype, filetype.getDefaultExtension());
    }

    @NotNull
    default CollectedTabContent contentWithFolding(@NotNull Tabs tab,
                                        boolean enabled,
                                        @NotNull FileType filetype) {
        return collect(tab, enabled, NO_ERROR, filetype, filetype.getDefaultExtension(), WITH_FOLDING, null);
    }
}
