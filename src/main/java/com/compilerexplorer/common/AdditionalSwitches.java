package com.compilerexplorer.common;

import org.jetbrains.annotations.NonNls;

import java.util.Collections;
import java.util.List;

public class AdditionalSwitches {
    @NonNls
    public static final List<String> INSTANCE = Collections.singletonList("-Wno-pedantic");
}
