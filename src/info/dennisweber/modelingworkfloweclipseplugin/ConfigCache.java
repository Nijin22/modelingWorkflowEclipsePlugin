package info.dennisweber.modelingworkfloweclipseplugin;

public class ConfigCache {
	private static String repoApiUrl;
	private static String jiraApiUrl;

	public static String getRepoApiUrl() {
		// Set defaults for prototype
		if (repoApiUrl == null) {
			repoApiUrl = "https://stash.bfs-intra.net/rest/api/1.0/users/denweb01/repos/masterthesis-example-model";
		}

		return repoApiUrl;
	}

	public static void setRepoApiUrl(String repoApiUrl) {
		ConfigCache.repoApiUrl = repoApiUrl;
	}

	public static String getJiraApiUrl() {
		// Set defaults for prototype
		if (jiraApiUrl == null) {
			jiraApiUrl = "https://jira.bfs-intra.net/rest/agile/latest/board/137/";
		}

		return jiraApiUrl;
	}

	public static void setJiraApiUrl(String jiraApiUrl) {
		ConfigCache.jiraApiUrl = jiraApiUrl;
	}

}
