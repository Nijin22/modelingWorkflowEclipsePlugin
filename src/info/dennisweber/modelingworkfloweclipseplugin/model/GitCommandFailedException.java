package info.dennisweber.modelingworkfloweclipseplugin.model;

import java.util.LinkedList;
import java.util.List;

public class GitCommandFailedException extends RuntimeException {
	private static final long serialVersionUID = 1L;
	private int exitCode;
	private String commandInput;
	private List<String> results = new LinkedList<String>();

	public GitCommandFailedException(int exitCode, String commandInput, List<String> results) {
		super();
		this.exitCode = exitCode;
		this.commandInput = commandInput;
		this.results = results;
	}

	public int getExitCode() {
		return exitCode;
	}

	public String getCommandInput() {
		return commandInput;
	}

	public List<String> getResults() {
		return results;
	}

	@Override
	public String toString() {
		return "GitCommandFailedException [exitCode=" + exitCode + ", commandInput=" + commandInput + ", results="
				+ results + "]";
	}

}
