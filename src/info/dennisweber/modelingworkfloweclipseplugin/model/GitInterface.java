package info.dennisweber.modelingworkfloweclipseplugin.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.eclipse.core.resources.IProject;

public class GitInterface {
	private IProject eclipseProject;
	private final String gitBaseCmd;

	public GitInterface(IProject eclipseProject) {
		this.eclipseProject = eclipseProject;
		gitBaseCmd = "git -C \"" + eclipseProject.getLocation() + "\"";
	}

	public Set<String> getReleaseBranches() {
		HashSet<String> releaseBranches = new HashSet<String>();
		try {
			List<String> branches = executeGitCommand("branch -r");
			for (String branch : branches) {
				branch = branch.trim();
				if (branch.startsWith("origin/release/")) {
					// this is a release branch
					releaseBranches.add(branch);
				}
			}
			return releaseBranches;
		} catch (IOException | InterruptedException e) {
			throw new RuntimeException(e);
		}

	}
	private List<String> executeGitCommand(String command) throws InterruptedException, IOException {
		List<String> results = new LinkedList<String>();

		String cmd = gitBaseCmd + " " + command;
		System.out.println("[GIT Input:] " + cmd);
		Process p = Runtime.getRuntime().exec(cmd);
		p.waitFor();
		BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
		String line;
		while ((line = r.readLine()) != null) {
			System.out.println("[GIT Output:] " + line);
			results.add(line);
		}

		return results;
	}
}
