package com.compilerexplorer.compiler.common;

import com.intellij.openapi.progress.ProgressIndicator;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.concurrent.TimeUnit;

public class CompilerRunner {
    @NotNull
    private final String stdout;
    @NotNull
    private final String stderr;
    private final int exitCode;

    public CompilerRunner(@NotNull String[] commandArray, @NotNull File workingDir, @NotNull String stdin, @NotNull ProgressIndicator progressIndicator) throws IOException, InterruptedException {
        Process process = Runtime.getRuntime().exec(commandArray, null, workingDir);
        OutputStream stdinStream = process.getOutputStream();
        stdinStream.write(stdin.getBytes());
        stdinStream.flush();
        stdinStream.close();
        stdout = read(process.getInputStream(), progressIndicator);
        stderr = read(process.getErrorStream(), progressIndicator);
        while (!process.waitFor(100, TimeUnit.MILLISECONDS)) {
            progressIndicator.checkCanceled();
        }
        exitCode = process.exitValue();
        progressIndicator.checkCanceled();
    }

    @NotNull
    public String getStdout() {
        return stdout;
    }

    @NotNull
    public String getStderr() {
        return stderr;
    }

    public int getExitCode() {
        return exitCode;
    }

    @NotNull
    private static String read(InputStream stream, @NotNull ProgressIndicator progressIndicator) throws IOException {
        BufferedReader reader = new BufferedReader(new InputStreamReader(stream));
        String result = "";
        String line;
        while ((line = reader.readLine()) != null) {
            result = result.concat(line).concat("\n");
            progressIndicator.checkCanceled();
        }
        reader.close();
        progressIndicator.checkCanceled();
        return result;
    }
}
