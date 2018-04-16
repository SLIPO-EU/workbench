package eu.slipo.workbench.web.service.etl;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.FutureTask;

import org.apache.commons.lang3.StringUtils;
import org.springframework.core.task.SimpleAsyncTaskExecutor;
import org.springframework.core.task.TaskExecutor;

/**
 * Utility class for executing shell commands.
 */
public class CommandExecutor {

    /**
     * The command to execute.
     */
    private String[] command;

    /**
     * Output file
     */
    private String outputFile;

    /**
     * Environment parameters to set during the command execution.
     */
    private Map<String, String> environmentParams = null;

    /**
     * Working directory of the process that executes the shell command.
     */
    private File workingDirectory = null;

    /**
     * Command execution timeout in milliseconds.
     */
    private long timeout = 0;

    /**
     * Timeout polling interval in milliseconds.
     */
    private long checkInterval = 2000;

    /**
     * Simple {@link TaskExecutor} for executing threads.
     */
    private TaskExecutor taskExecutor = new SimpleAsyncTaskExecutor();

    /**
     * True if the thread executing the task should be interrupted when timeout expires.
     */
    private boolean interruptOnCancel = false;

    /**
     * The process that executes the command.
     */
    private Process process;

    /**
     * Stores shell output. The caller may parse the output to decide if script has been
     * executed successfully.
     */
    private List<String> output = new ArrayList<String>();

    /**
     * Creates a new {@link CommandExecutor}.
     *
     * @param command the command to execute.
     * @param timeout execution timeout in milliseconds.
     * @param
     */
    public CommandExecutor(String[] command, long timeout) {
        this.command = command;
        this.timeout = timeout;
    }

    /**
     * Creates a new {@link CommandExecutor}.
     *
     * @param command the command to execute.
     * @param timeout execution timeout in milliseconds.
     * @param outputFile file to redirect output
     */
    public CommandExecutor(String[] command, long timeout, String outputFile) {
        this.command = command;
        this.timeout = timeout;
        this.outputFile = outputFile;
    }

    /**
     * Creates a new {@link CommandExecutor}.
     *
     * @param command the command to execute.
     * @param timeout execution timeout in milliseconds.
     * @param workingDirectory the working directory.
     */
    public CommandExecutor(String[] command, long timeout, String outputFile, String workingDirectory) {
        this.command = command;
        this.timeout = timeout;
        this.outputFile = outputFile;
        this.workingDirectory = new File(workingDirectory);
    }

    /**
     * Creates a new {@link CommandExecutor}.
     *
     * @param command the command to execute.
     * @param timeout execution timeout in milliseconds.
     * @param workingDirectory the working directory.
     * @param environmentParams the environment variables to set.
     */
    public CommandExecutor(String[] command, long timeout, String workingDirectory, Map<String, String> environmentParams) {
        this.command = command;
        this.timeout = timeout;
        this.workingDirectory = new File(workingDirectory);
        this.environmentParams = environmentParams;
    }

    /**
     * Executes the command.
     *
     * @return the {@link ExitStatus} of the command execution.
     * @throws Exception if command fails.
     */
    public int execute() throws Exception {
        ProcessBuilder builder = new ProcessBuilder(command);
        if (workingDirectory != null) {
            builder.directory(workingDirectory);
        }
        if (environmentParams != null) {
            Map<String, String> environment = builder.environment();
            for (String key : environmentParams.keySet()) {
                environment.put(key, environmentParams.get(key));
            }
        }
        if (!StringUtils.isBlank(this.outputFile)) {
            builder.redirectOutput(new File(this.outputFile));
        }

        FutureTask<Integer> systemCommandTask = new FutureTask<Integer>(new Callable<Integer>() {

            @Override
            public Integer call() throws Exception {
                process = builder.start();
                return process.waitFor();
            }

        });

        long startTime = System.currentTimeMillis();

        taskExecutor.execute(systemCommandTask);

        while (true) {
            Thread.sleep(checkInterval);

            if (systemCommandTask.isDone()) {
                int result = systemCommandTask.get();
                readOutput(result);
                return result;
            } else if (System.currentTimeMillis() - startTime > timeout) {
                systemCommandTask.cancel(interruptOnCancel);
                throw new Exception("Execution of system command did not finish within the timeout.");
            }
        }
    }

    public String[] getCommand() {
        return command;
    }

    public Map<String, String> getEnvironmentParams() {
        return environmentParams;
    }

    public File getWorkingDirectory() {
        return workingDirectory;
    }

    public long getTimeout() {
        return timeout;
    }

    public String[] getOutput() {
        return output.toArray(new String[] {});
    }

    /**
     * Reads the shell command output.
     *
     * @throws Exception if an I/O error occurs.
     */
    private void readOutput(int exitCode) throws Exception {
        output.clear();

        BufferedReader reader = new BufferedReader(new InputStreamReader(exitCode == 0 ? process.getInputStream() : process.getErrorStream()));

        String line = "";
        while ((line = reader.readLine()) != null) {
            output.add(line);
        }
    }

}
