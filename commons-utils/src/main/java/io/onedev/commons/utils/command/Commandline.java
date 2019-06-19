package io.onedev.commons.utils.command;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.base.Preconditions;

import io.onedev.commons.utils.StringUtils;

public class Commandline  {
	
    static final ExecutorService EXECUTOR_SERVICE = Executors.newCachedThreadPool();

	private static final Logger logger = LoggerFactory.getLogger(Commandline.class);

    private String executable;
    
    private List<String> arguments = new ArrayList<String>();
    
    private File workingDir;
    
    private Map<String, String> environments = new HashMap<String, String>();
    
    public Commandline(String executable) {
    	Preconditions.checkNotNull(executable);
        this.executable = executable.replace('/', File.separatorChar).replace('\\', File.separatorChar);
    }
    
    public Commandline arguments(List<String> arguments) {
    	this.arguments.clear();
    	this.arguments.addAll(arguments);
    	return this;
    }
    
    public Commandline addArgs(String... args) {
    	for (String each: args)
    		arguments.add(each);
    	return this;
    }
    
    public Commandline workingDir(File workingDir) {
    	this.workingDir = workingDir;
    	return this;
    }
    
    public Commandline environments(Map<String, String> environments) {
    	this.environments.clear();
    	this.environments.putAll(environments);
    	return this;
    }

    public String toString() {
    	List<String> command = new ArrayList<String>();
    	command.add(executable);
    	command.addAll(arguments);

    	StringBuffer buf = new StringBuffer();
        for (String each: command) {
        	if (each.contains(" ") || each.contains("\t")) {
        		buf.append("\"").append(StringUtils.replace(
        				each, "\n", "\\n")).append("\"").append(" ");
        	} else {
        		buf.append(StringUtils.replace(
        				each, "\n", "\\n")).append(" ");
        	}
        }
        return buf.toString();
    }

    public Commandline clearArgs() {
        arguments.clear();
        return this;
    }
    
	private ProcessBuilder createProcessBuilder(@Nullable Logger logger) {
		File workingDir = this.workingDir;
		if (workingDir == null)
			workingDir = new File(".");
		
		String executable = this.executable;
		
        if (!new File(executable).isAbsolute()) {
            if (new File(workingDir, executable).isFile())
            	executable = new File(workingDir, executable).getAbsolutePath();
            else if (new File(workingDir, executable + ".exe").isFile())
            	executable = new File(workingDir, executable + ".exe").getAbsolutePath();
            else if (new File(workingDir, executable + ".bat").isFile())
            	executable = new File(workingDir, executable + ".bat").getAbsolutePath();
            else if (new File(workingDir, executable + ".cmd").isFile())
            	executable = new File(workingDir, executable + ".cmd").getAbsolutePath();
        }

		List<String> command = new ArrayList<String>();
		command.add(executable);
		command.addAll(arguments);
		
        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.directory(workingDir);
        
        processBuilder.environment().putAll(environments);
        
		if (logger == null)
			logger = Commandline.logger;
		
        if (logger.isDebugEnabled()) {
    		logger.trace("Executing command: " + this);
    		logger.trace("Command working directory: " + 
    				processBuilder.directory().getAbsolutePath());
    		StringBuffer buffer = new StringBuffer();
    		for (Map.Entry<String, String> entry: processBuilder.environment().entrySet())
    			buffer.append("	" + entry.getKey() + "=" + entry.getValue() + "\n");
    		logger.trace("Command execution environments:\n" + 
    				StringUtils.stripEnd(buffer.toString(), "\n"));
    	}

    	return processBuilder;
    }
    
	public ExecuteResult execute(@Nullable OutputStream stdout, @Nullable LineConsumer stderr, @Nullable InputStream stdin) {
		return execute(stdout, stderr, stdin, null);
	}
			
	public ExecuteResult execute(@Nullable OutputStream stdout, @Nullable LineConsumer stderr, @Nullable InputStream stdin, 
			@Nullable Logger logger) {
		return execute(stdout, stderr, stdin, new ProcessKiller() {
			
			@Override
			public void kill(Process process) {
				process.destroy();
			}
			
		}, logger);
	}
	
	/**
	 * Execute the command.
	 * 
	 * @param stdout
	 * 			output stream to write standard output, caller is responsible for closing the stream
	 * @param stderr
	 * 			line consumer to handle standard error
	 * @param stdin
	 * 			input stream to read standard input from, caller is responsible for closing the stream
	 * @return
	 * 			execution result
	 */
	public ExecuteResult execute(@Nullable OutputStream stdout, @Nullable LineConsumer stderr, @Nullable InputStream stdin, 
			ProcessKiller processKiller, @Nullable Logger logger) {
    	Process process;
        try {
        	ProcessBuilder processBuilder = createProcessBuilder(logger);
        	process = processBuilder.redirectErrorStream(stderr == null).start();
        } catch (IOException e) {
        	throw new RuntimeException(e);
        }

    	final StringBuffer errorMessage = new StringBuffer();
		OutputStream errorMessageCollector = null;
		if (stderr != null) {
			errorMessageCollector = new LineConsumer(stderr.getEncoding()) {

				@Override
				public void consume(String line) {
					if (errorMessage.length() != 0)
						errorMessage.append("\n");
					errorMessage.append(line);
					stderr.consume(line);
				}
				
			};
		}
    	
        ProcessStreamPumper streamPumper = new ProcessStreamPumper(process, stdout, errorMessageCollector, stdin);
        
        ExecuteResult result = new ExecuteResult(this);
        try {
            result.setReturnCode(process.waitFor());
		} catch (InterruptedException e) {
			processKiller.kill(process);
			throw new RuntimeException(e);
		} finally {
			streamPumper.waitFor();
		}

        if (errorMessage.length() != 0)
        	result.setErrorMessage(errorMessage.toString());
        
        return result;
    }
    
	/**
	 * Execute the command.
	 * 
	 * @param stdout
	 * 			output stream to write standard output, caller is responsible for closing the stream
	 * @param stderr
	 * 			line consumer to handle standard error
	 * @return
	 * 			execution result
	 */
    public ExecuteResult execute(@Nullable OutputStream stdout, @Nullable LineConsumer stderr) {
    	return execute(stdout, stderr, null);
    }

}