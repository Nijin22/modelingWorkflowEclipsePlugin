package info.dennisweber.modelingworkfloweclipseplugin;

public class ConfigCache {
	private boolean isConfigured = false;
	private String repoApiUrl;
	private String jiraApiUrl;
	private String username;
	private String password; // Cleartext!

	public void update(String repoApiUrl, String jiraApiUrl, String username, String password) {
		// TODO: Verify entered data is valid
		this.repoApiUrl = repoApiUrl;
		this.jiraApiUrl = jiraApiUrl;
		this.username = username;
		this.password = password;

		this.isConfigured = true;
	}

	public String getUsername() {
		if (username == null) {
			username = "denweb01";
		}
		return username;
	}

	public String getPassword() {
		if (password == null) {
			password = "";
		}
		return password;
	}

	public String getRepoApiUrl() {
		// Set defaults for prototype
		if (repoApiUrl == null) {
			repoApiUrl = "https://stash.bfs-intra.net/rest/api/1.0/users/denweb01/repos/masterthesis-example-model";
		}

		return repoApiUrl;
	}

	public String getJiraApiUrl() {
		// Set defaults for prototype
		if (jiraApiUrl == null) {
			jiraApiUrl = "https://jira.bfs-intra.net/rest/agile/1.0/board/137/";
		}

		return jiraApiUrl;
	}

	public boolean isConfigured() {
		return isConfigured;
	}

}
