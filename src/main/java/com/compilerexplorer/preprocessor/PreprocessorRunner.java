package com.compilerexplorer.preprocessor;

import com.intellij.openapi.progress.ProgressIndicator;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import java.util.concurrent.TimeUnit;

class PreprocessorRunner {
    @NotNull
    private final String stdout;
    @NotNull
    private final String stderr;
    private final int exitCode;

    PreprocessorRunner(@NotNull String commandLine, @NotNull File workingDir, @NotNull String stdin, @NotNull ProgressIndicator progressIndicator) throws IOException, InterruptedException {
        Process process = Runtime.getRuntime().exec(commandLine, null, workingDir);
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
    String getStdout() {
        return stdout;
    }

    @NotNull
    String getStderr() {
        return stderr;
    }

    int getExitCode() {
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
