package com.compilerexplorer.compiler.common;

import com.intellij.openapi.progress.ProgressIndicator;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.*;

public class CompilerRunner {
    @NotNull
    private final String stdout;
    @NotNull
    private final String stderr;
    private final int exitCode;

    public CompilerRunner(@NotNull String[] commandArray, @NotNull File workingDir, @NotNull String stdin, @NotNull ProgressIndicator progressIndicator) throws IOException, InterruptedException {
        Process process = Runtime.getRuntime().exec(commandArray, null, workingDir);

        try {
            OutputStream stdinStream = process.getOutputStream();
            stdinStream.write(stdin.getBytes());
            stdinStream.flush();
            stdinStream.close();

            StringBuilder stdoutBuilder = new StringBuilder();
            StringBuilder stderrBuilder = new StringBuilder();
            StreamGobbler stdoutGobbler = new StreamGobbler(process.getInputStream(), stdoutBuilder, progressIndicator);
            StreamGobbler stderrGobbler = new StreamGobbler(process.getErrorStream(), stderrBuilder, progressIndicator);
            Thread stdoutGobblerThread = new Thread(stdoutGobbler);
            Thread stderrGobblerThread = new Thread(stderrGobbler);
            stdoutGobblerThread.start();
            stderrGobblerThread.start();

            exitCode = process.waitFor();
            stdoutGobblerThread.join();
            stderrGobblerThread.join();

            if (stdoutGobbler.getException() != null) {
                throw new InterruptedException(stdoutGobbler.getException().getMessage());
            }
            if (stderrGobbler.getException() != null) {
                throw new InterruptedException(stderrGobbler.getException().getMessage());
            }

            progressIndicator.checkCanceled();
            stdout = stdoutBuilder.toString().replaceAll("\r\n?", "\n");
            stderr = stderrBuilder.toString().replaceAll("\r\n?", "\n");
        } catch (Exception exception) {
            process.destroyForcibly();
            throw exception;
        }
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

    private static class StreamGobbler implements Runnable {
        @NotNull
        private final InputStream inputStream;
        @NotNull
        private final StringBuilder stringBuilder;
        @NotNull
        private final ProgressIndicator progressIndicator;
        @Nullable
        private Exception exception;

        StreamGobbler(@NotNull InputStream inputStream_, @NotNull StringBuilder stringBuilder_, @NotNull ProgressIndicator progressIndicator_) {
            inputStream = inputStream_;
            stringBuilder = stringBuilder_;
            progressIndicator = progressIndicator_;
        }

        public void run() {
            try {
                int c;
                while ((c = inputStream.read()) != -1) {
                    stringBuilder.append((char) c);
                    progressIndicator.checkCanceled();
                }
            } catch (IOException x) {
                exception = x;
            }
        }

        @Nullable
        Exception getException() {
            return exception;
        }
    }
}
