package info.dennisweber.modelingworkfloweclipseplugin.model;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import okhttp3.Authenticator;
import okhttp3.Credentials;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.RequestBody;
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
			String url = configCache.getJiraApiUrl() + "agile/1.0/board/" + configCache.getJiraBoardId() + "/sprint";
			String json = makeRequest(RequestType.GET, url, "");

			// Find active sprint and CACHE!
			JsonObject jsonObj = new JsonParser().parse(json).getAsJsonObject();
			jsonObj.get("values").getAsJsonArray().forEach(sprint -> {
				if (sprint.getAsJsonObject().get("state").getAsString().equals("active")) {
					// this is the active string
					activeSprintCached = sprint.getAsJsonObject().get("id").getAsInt();
				}
			});
		}
		return activeSprintCached;
	}

	public List<Issue> getIssues() throws IOException {
		LinkedList<Issue> issues = new LinkedList<Issue>();

		String url = configCache.getJiraApiUrl() + "agile/1.0/board/" + configCache.getJiraBoardId() + "/sprint" + "/"
				+ getActiveSprint() + "/" + "issue?maxResults=999999";
		// Current limit for jira.search.views.default.max at ACTICO is 1000, but if you
		// have more than 1000 issues in your
		// sprint, you probably have other problems than a half-working-prototype.

		String json = makeRequest(RequestType.GET, url, "");
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

	public void moveIssueInProgress(String issueId) throws IOException {
		String url = configCache.getJiraApiUrl() + "api/2/issue/" + issueId + "/transitions";
		String json = "{\"transition\":{\"id\":\"4\"}}"; // "Transition it via 4 (Open --> In Progress)
		makeRequest(RequestType.POST, url, json);
	}

	private enum RequestType {
		GET, POST
	};

	private String makeRequest(RequestType type, String url, String bodyJson) throws IOException {
		System.out.println("[Requesting] " + url);

		Builder rb = new Request.Builder();
		rb.url(url);
		if (type == RequestType.POST) {
			final MediaType JSON = MediaType.parse("application/json; charset=utf-8");
			RequestBody body = RequestBody.create(JSON, bodyJson);
			rb.post(body);
		}
		Request request = rb.build();

		Response response = client.newCall(request).execute();

		if (response.isSuccessful()) {
			String answer = response.body().string();
			// System.out.println("[Response (" + response.code() + ")] " + answer);
			return answer;
		} else {
			System.out.println(response.body().string());
			throw new RuntimeException("Call failed. (" + response.code() + ")");
		}

	}

	private int responseCount(Response response) {
		int result = 1;
		while ((response = response.priorResponse()) != null) {
			result++;
		}
		return result;
	}
}