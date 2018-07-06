package info.dennisweber.modelingworkfloweclipseplugin.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;

public class GitInterface {
	private IProject eclipseProject;

	public GitInterface(IProject eclipseProject) {
		this.eclipseProject = eclipseProject;
	}

	public Set<String> getReleaseBranches() {
		HashSet<String> releaseBranches = new HashSet<String>();
		try {
			String cmd = "git -C \"" + eclipseProject.getLocation() + "\" branch -r"; // Get remote branches
			Process p = Runtime.getRuntime().exec(cmd);
			p.waitFor();
			BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()));
			String line;
			while ((line = r.readLine()) != null) {
				line = line.trim();
				if (line.startsWith("origin/release/")) {
					// this is a release branch
					releaseBranches.add(line);
				}
			}

			return releaseBranches;
		} catch (IOException | InterruptedException e) {
			throw new RuntimeException(e);
		}

	}
}
