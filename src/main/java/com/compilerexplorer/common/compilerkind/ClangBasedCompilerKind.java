package com.compilerexplorer.common.compilerkind;

import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ClangBasedCompilerKind extends GccLikeCompilerKind {
    @NonNls
    @NotNull
    private static final String ICX_KIND = "icx";
    @NonNls
    @NotNull
    private static final String ICX_VERSION_BANNER = "Intel(R) oneAPI DPC++/C++ Compiler";

    public ClangBasedCompilerKind(@NonNls @NotNull String compilerKind, @NonNls @NotNull List<String> executableFilenames_) {
        super(compilerKind, executableFilenames_);
    }

    @Override
    @NonNls
    @NotNull
    public String parseCompilerName(@NonNls @NotNull String versionText) {
        if (isIcx(versionText)) {
            return ICX_KIND;
        } else {
            return super.parseCompilerName(versionText);
        }
    }

    @Override
    @NonNls
    @NotNull
    protected String parseCompilerVersion(@NonNls @NotNull String compilerKind, @NonNls @NotNull String versionText) {
        if (isIcx(versionText)) {
            return versionText.replace('\n', ' ').replaceAll(".* Compiler ([^ ]*) .*", "$1");
        } else {
            return super.parseCompilerVersion(compilerKind, versionText);
        }
    }

    private static boolean isIcx(@NonNls @NotNull String versionText) {
        return versionText.contains(ICX_VERSION_BANNER);
    }
}
