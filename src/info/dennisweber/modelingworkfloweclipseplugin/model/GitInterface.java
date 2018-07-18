package info.dennisweber.modelingworkfloweclipseplugin.model;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
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

	public void createBranch(String baseBranch, String newBranchName) {
		try {
			// Checkout base branch
			executeGitCommand("checkout " + baseBranch);

			// Create (and checkout) the new branch
			executeGitCommand("checkout -b " + newBranchName);
		} catch (InterruptedException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void checkout(String branchName) {
		try {
			fetch();
			
			if (doesBranchExist(branchName)) {
				executeGitCommand("checkout " + branchName);
			} else {
				executeGitCommand("checkout -b " + branchName);
			}
		} catch (InterruptedException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	public boolean doesBranchExist(String branchName) {
		try {
			List<String> res = executeGitCommand("git show-ref refs/heads/" + branchName);
			if (res.isEmpty()) {
				// Nothing found
				return false;
			} else {
				// Content would be the SHA of the branch
				return true;
			}
		} catch (InterruptedException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	public String getCurrentBranch() {
		// https://stackoverflow.com/a/12142066/3298787
		try {
			return executeGitCommand("rev-parse --abbrev-ref HEAD").get(0);
		} catch (InterruptedException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void fetch() {
		try {
			executeGitCommand("fetch");
		} catch (InterruptedException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	public List<CommitDto> getLog() {
		List<CommitDto> commits = new LinkedList<CommitDto>();
		try {
			final String separator = "/"; // Separator for git. May not appear in Hash or time.
			List<String> result = executeGitCommand("log --format=\"%H" + separator + "%ar" + separator + "%s\"");
			for (String commitLine : result) {
				String[] splitted = commitLine.split(separator, 3);
				CommitDto commit = new CommitDto();
				commit.hash = splitted[0];
				commit.relativeTime = splitted[1];
				commit.message = splitted[2];
				commits.add(commit);
			}
		} catch (InterruptedException | IOException e) {
			throw new RuntimeException(e);
		}
		return commits;
	}

	public List<String> getNotIndexedFiles() {
		try {
			List<String> files = new LinkedList<String>();

			// Get New files
			files.addAll(executeGitCommand("ls-files --others --exclude-standard"));

			// Get Modified Files
			files.addAll(executeGitCommand("diff --name-only")); // Differences between Working Tree and Index

			return files;
		} catch (InterruptedException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	public List<String> getIndexedFiles() {
		try {
			return executeGitCommand("diff --name-only --cached"); // Differences between Index and HEAD
		} catch (InterruptedException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void addAll() {
		try {
			executeGitCommand("add .");
		} catch (InterruptedException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void addFile(String filePath) {
		try {
			executeGitCommand("add " + filePath);
		} catch (InterruptedException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void unstageAll() {
		try {
			executeGitCommand("reset --mixed");
		} catch (InterruptedException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void unstageFile(String filePath) {
		try {
			executeGitCommand("reset " + filePath);
		} catch (InterruptedException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void commit(String commitMsg) {
		try {
			final String COMMIT_MSG_FILE = eclipseProject.getLocation() + "/.git/COMMIT_EDITMSG";

			// Write commit message to file
			Files.write(Paths.get(COMMIT_MSG_FILE), commitMsg.getBytes());

			executeGitCommand("commit --file=\"" + COMMIT_MSG_FILE + "\"");
		} catch (InterruptedException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void push(String remoteBranchName, boolean force) {
		try {
			String forceInsert = "";
			if (force) {
				forceInsert = "--force ";
			}
			executeGitCommand("push " + forceInsert + "origin " + remoteBranchName);
		} catch (InterruptedException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	public void hardReset(String commitIdentifier) {
		try {
			executeGitCommand("reset --hard " + commitIdentifier);
		} catch (InterruptedException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	public List<String> getChangedFilesBetweenTwoBranches(String baseBranch, String secondBranch) {
		try {
			String cmd = "diff --name-only " + baseBranch + ".." + secondBranch;
			return executeGitCommand(cmd);
		} catch (InterruptedException | IOException e) {
			throw new RuntimeException(e);
		}
	}

	private List<String> executeGitCommand(String command) throws InterruptedException, IOException {
		List<String> results = new LinkedList<String>();

		String cmd = "git "; // base command
		cmd += "-C \"" + eclipseProject.getLocation() + "\" "; // Operate in project folder
		cmd += "--no-optional-locks "; // Don't perform optional operations, which would require locks
		cmd += command; // The actual command

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
