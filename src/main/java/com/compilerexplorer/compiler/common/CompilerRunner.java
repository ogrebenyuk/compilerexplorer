package com.compilerexplorer.compiler.common;

import com.intellij.execution.ExecutionException;
import com.intellij.openapi.progress.ProgressIndicator;
import com.jetbrains.cidr.execution.CidrRunProcessUtil;
import org.jetbrains.annotations.NotNull;

import java.io.*;
import com.intellij.execution.configurations.GeneralCommandLine;
import com.intellij.execution.process.*;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.openapi.util.ShutDownTracker;
import com.jetbrains.cidr.cpp.cmake.workspace.CMakeProfileInfo;
import com.jetbrains.cidr.cpp.cmake.workspace.CMakeWorkspace;
import com.jetbrains.cidr.cpp.execution.CMakeAppRunConfiguration;
import com.jetbrains.cidr.cpp.execution.CMakeBuildProfileExecutionTarget;
import com.jetbrains.cidr.cpp.toolchains.CPPEnvironment;
import com.jetbrains.cidr.lang.workspace.OCResolveConfiguration;
import com.jetbrains.cidr.system.HostMachine;

public class CompilerRunner {
    @NotNull
    private final String stdout;
    @NotNull
    private final String stderr;
    private final int exitCode;

    public static HostMachine getHostMachine(@NotNull OCResolveConfiguration configuration_) {
        try {
            CMakeAppRunConfiguration runConfiguration = CMakeAppRunConfiguration.getSelectedRunConfiguration(configuration_.getProject());
            CMakeBuildProfileExecutionTarget executionTarget = CMakeAppRunConfiguration.getSelectedBuildProfile(configuration_.getProject());
            assert runConfiguration != null;
            assert executionTarget != null;
            CMakeAppRunConfiguration.BuildAndRunConfigurations buildAndRunConfiguration = runConfiguration.getBuildAndRunConfigurations(executionTarget);
            CMakeWorkspace cMakeWorkspace = CMakeWorkspace.getInstance(configuration_.getProject());
            assert buildAndRunConfiguration != null;
            final CMakeProfileInfo cMakeProfileInfo = cMakeWorkspace.getProfileInfoFor(buildAndRunConfiguration.buildConfiguration);
            final CPPEnvironment environment = cMakeProfileInfo.getEnvironment();
            assert environment != null;
            return environment.getHostMachine();
        } catch (ExecutionException e) {
            throw new RuntimeException("Fatal error");
        }
    }

    public CompilerRunner(@NotNull OCResolveConfiguration configuration_, @NotNull String[] commandArray, @NotNull File workingDir, @NotNull String stdin, @NotNull ProgressIndicator progressIndicator, int compilerTimeoutMillis) {
        ProcessOutput output;
        try {
            final HostMachine host = getHostMachine(configuration_);

            GeneralCommandLine cl = new GeneralCommandLine();
            cl.setExePath(commandArray[0]);
            cl.setWorkDirectory(workingDir);
            cl.setRedirectErrorStream(false);
            for (String parameter : commandArray) {
                if (parameter.equals(commandArray[0]))
                    continue;
                cl.addParameter(parameter);
            }
            final BaseProcessHandler<?> process = host.createProcess(cl, false, false);
            Runnable shutdownHook = () -> host.killProcessTree(process);
            ShutDownTracker.getInstance().registerShutdownTask(shutdownHook);
            OutputStream stdinStream = process.getProcess().getOutputStream();
            stdinStream.write(stdin.getBytes());
            stdinStream.flush();
            stdinStream.close();

            try {
                output = CidrRunProcessUtil.runProcess(process, progressIndicator, compilerTimeoutMillis);
                if (output.isCancelled()) {
                    throw new ProcessCanceledException();
                }
            } finally {
                ShutDownTracker.getInstance().unregisterShutdownTask(shutdownHook);
                shutdownHook.run();
            }


        }
        catch (Exception e) {
            throw(new RuntimeException("Failed to run compiler: "+e.getMessage()));
        }

        stdout = output.getStdout();
        stderr = output.getStderr();
        exitCode = output.isExitCodeSet() ? output.getExitCode() : 0;
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
}
