package com.compilerexplorer.common;

public class SuppressionFlag {
    private boolean flag = false;

    public void unlessApplied(Runnable runnable) {
        if (!flag) {
            runnable.run();
        }
    }

    public void apply(Runnable runnable) {
        flag = true;
        try {
            runnable.run();
        } finally {
            flag = false;
        }
    }
}
