package info.dennisweber.modelingworkfloweclipseplugin.model;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import info.dennisweber.modelingworkfloweclipseplugin.ConfigCache;
import okhttp3.Authenticator;
import okhttp3.Credentials;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.Route;

public class JiraRestApi {
	private OkHttpClient client;
	private ConfigCache configCache;

	private int activeSprintCached = -1;

	public JiraRestApi(ConfigCache configCache) {
		this.configCache = configCache;

		// Use authentication
		// See: https://stackoverflow.com/a/34819354/3298787
		OkHttpClient.Builder builder = new OkHttpClient.Builder();
		builder.authenticator(new Authenticator() {

			@Override
			public Request authenticate(Route route, Response response) throws IOException {
				if (responseCount(response) >= 3) {
					System.out.println("DEBUG: Giving up on authentication.");
					return null; // If we've failed 3 times, give up.
				}
				String credential = Credentials.basic(configCache.getUsername(), configCache.getPassword());
				return response.request().newBuilder().header("Authorization", credential).build();
			}
		});

		client = builder.build();
	}

	public int getActiveSprint() throws IOException {
		if (activeSprintCached == -1) {
			// Not cached yet.
			String url = configCache.getJiraApiUrl() + "sprint";
			Request request = new Request.Builder().url(url).build();
			Response response = client.newCall(request).execute();
			String json = response.body().string();

			// Find active sprint and CACHE!
			JsonObject jsonObj = new JsonParser().parse(json).getAsJsonObject();
			jsonObj.get("values").getAsJsonArray().forEach(sprint -> {
				if (sprint.getAsJsonObject().get("state").getAsString().equals("active")) {
					// this is the active string
					activeSprintCached = sprint.getAsJsonObject().get("id").getAsInt();
				}
			});
		}

		System.out.println("Debug: Active Sprint is " + activeSprintCached);
		return activeSprintCached;

	}

	public List<Issue> getIssues() throws IOException {
		LinkedList<Issue> issues = new LinkedList<Issue>();

		String url = configCache.getJiraApiUrl() + "sprint" + "/" + getActiveSprint() + "/" + "issue?maxResults=999999";
		// Current limit for ira.search.views.default.max at ACTICO is 1000, but if you
		// have more than 1000 issues in your
		// sprint, you probably have other problems than a half-working-prototype.
		Request request = new Request.Builder().url(url).build();
		Response response = client.newCall(request).execute();
		String json = response.body().string();

		JsonObject jsonObj = new JsonParser().parse(json).getAsJsonObject();
		jsonObj.get("issues").getAsJsonArray().forEach(issueJsonElement -> {
			JsonObject issueJsonObject = issueJsonElement.getAsJsonObject();

			String id = issueJsonObject.get("key").getAsString();
			String title = issueJsonObject.get("fields").getAsJsonObject().get("summary").getAsString();
			String assignee = issueJsonObject.get("fields").getAsJsonObject().get("assignee").getAsJsonObject()
					.get("displayName").getAsString();
			IssueStatus status;
			String statusText = issueJsonObject.get("fields").getAsJsonObject().get("status").getAsJsonObject()
					.get("statusCategory").getAsJsonObject().get("key").getAsString();
			switch (statusText) {
			case "new":
				status = IssueStatus.ToDo;
				break;
			case "indeterminate":
				status = IssueStatus.InProgress;
				break;
			case "done":
				status = IssueStatus.Done;
				break;
			default:
				throw new RuntimeException("Unexpected status >" + statusText + "< for issue >" + id + "<.");
			}

			Issue issue = new Issue(id, title, status, assignee);
			issues.add(issue);

		});
		return issues;
	}

	private int responseCount(Response response) {
		int result = 1;
		while ((response = response.priorResponse()) != null) {
			result++;
		}
		return result;
	}
}
