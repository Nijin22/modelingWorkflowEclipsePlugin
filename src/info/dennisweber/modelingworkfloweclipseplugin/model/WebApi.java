package info.dennisweber.modelingworkfloweclipseplugin.model;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

import javax.xml.ws.WebServiceException;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import info.dennisweber.modelingworkfloweclipseplugin.model.PrDto.Reference;
import okhttp3.Authenticator;
import okhttp3.Credentials;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Request.Builder;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.Route;

public class WebApi {
	private OkHttpClient client;
	private ConfigCache configCache;
	private Gson gson = new Gson();

	private int activeSprintCached = -1;

	public static boolean TestConfigCache(ConfigCache cacheToTest) {
		OkHttpClient testClient = createClient(cacheToTest);
		String url;

		try {
			// Check bitbucket
			url = cacheToTest.getBbBaseUrl() + "/rest/api/1.0" + cacheToTest.getBbRepoPath();
			makeRequest(testClient, RequestType.GET, url, "");

			// Check Jira
			url = cacheToTest.getJiraUrl() + "/rest/agile/1.0/board/" + cacheToTest.getJiraBoardId();
			makeRequest(testClient, RequestType.GET, url, "");
		} catch (Exception e) {
			return false;
		}

		return true;
	}

	public WebApi(ConfigCache configCache) {
		client = createClient(configCache);
		this.configCache = configCache;
	}

	public int getActiveSprint() throws IOException {
		if (activeSprintCached == -1) {
			// Not cached yet.
			String url = configCache.getJiraUrl() + "/rest/agile/1.0/board/" + configCache.getJiraBoardId() + "/sprint";
			String json = makeRequest(client, RequestType.GET, url, "");

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

		String url = configCache.getJiraUrl() + "/rest/agile/1.0/board/" + configCache.getJiraBoardId() + "/sprint"
				+ "/" + getActiveSprint() + "/" + "issue?maxResults=999999";
		// Current limit for jira.search.views.default.max at ACTICO is 1000, but if you
		// have more than 1000 issues in your
		// sprint, you probably have other problems than a half-working-prototype.

		String json = makeRequest(client, RequestType.GET, url, "");
		JsonObject jsonObj = new JsonParser().parse(json).getAsJsonObject();
		jsonObj.get("issues").getAsJsonArray().forEach(issueJsonElement -> {
			JsonObject issueJsonObject = issueJsonElement.getAsJsonObject();

			String id = issueJsonObject.get("key").getAsString();
			String title = issueJsonObject.get("fields").getAsJsonObject().get("summary").getAsString();
			String assignee = extractAssignee(issueJsonObject);
			IssueStatus status = extractStatus(issueJsonObject);

			Issue issue = new Issue(id, title, status, assignee);
			issues.add(issue);

		});
		return issues;
	}

	public Issue getIssue(String issueId) throws IOException {
		String url = configCache.getJiraUrl() + "/rest/api/2/issue/" + issueId;
		String json = makeRequest(client, RequestType.GET, url, "");
		JsonObject jsonObj = new JsonParser().parse(json).getAsJsonObject();

		String title = jsonObj.get("fields").getAsJsonObject().get("summary").getAsString();
		IssueStatus status = extractStatus(jsonObj);
		String assignee = extractAssignee(jsonObj);

		return new Issue(issueId, title, status, assignee);
	}

	public void moveIssueInProgress(String issueId) throws IOException {
		performIssueTransition(issueId, 4); // Transition it via 4 (Open --> In Progress)
	}

	public void moveIssueInReview(String issueId) throws IOException {
		performIssueTransition(issueId, 731); // Transition it via 731 (--> To be reviewed)
	}

	public PrDto createPr(String sourceBranch, String targetBranch) throws IOException {
		String url = configCache.getBbBaseUrl() + "/rest/api/1.0" + configCache.getBbRepoPath() + "/pull-requests";
		
		PrDto pr = new PrDto();
		pr.title = "Merge >" + sourceBranch + "< into >" + targetBranch + "<.";
		pr.description = "This PR was automatically created by Dennis' modelling workflow prototype.";
		pr.state = "OPEN";
		pr.open = true;
		pr.closed = false;
		pr.fromRef = new Reference();
		pr.fromRef.id = "refs/heads/" + sourceBranch;
		pr.toRef = new Reference();
		pr.toRef.id = "refs/heads/" + targetBranch;
		
		String json = gson.toJson(pr, PrDto.class);
		String answer = makeRequest(client, RequestType.POST, url, json);
		
		PrDto returned = gson.fromJson(answer, PrDto.class);
		return returned;
	}

	private enum RequestType {
		GET, POST
	};

	private IssueStatus extractStatus(JsonObject issueJsonObject) {
		IssueStatus status;
		String statusId = issueJsonObject.get("fields").getAsJsonObject().get("status").getAsJsonObject().get("id")
				.getAsString();
		switch (statusId) {

		case "1": // Jira calls it "Open"
		case "4": // Jira calls it "Reopened"
		case "10000": // Jira calls it "To Be Refined"
		case "11000": // Jira calls it "To Do"
			status = IssueStatus.ToDo;
			break;

		case "3": // Jira calls it "In Progress"
			status = IssueStatus.InProgress;
			break;

		case "5": // Jira calls it "Resolved"
		case "6": // Jira calls it "Closed"
		case "10600": // Jira calls it "Done"
			status = IssueStatus.Done;
			break;

		case "10001": // Jira calls it "To Be Reviewed"
		case "10100": // Jira calls it "Test"
		case "10900": // Jira calls it "In Review"
			status = IssueStatus.InReview;
			break;

		default:
			throw new RuntimeException("Unexpected status >" + statusId + "<.");
		}
		return status;
	}

	private String extractAssignee(JsonObject jsonObj) {
		JsonElement assigneeElement = jsonObj.get("fields").getAsJsonObject().get("assignee");
		if (assigneeElement != null && !assigneeElement.isJsonNull()) {
			return assigneeElement.getAsJsonObject().get("displayName").getAsString();
		} else {
			return "[Not assigned]";
		}
	}

	private static OkHttpClient createClient(ConfigCache configCache) {
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

		return builder.build();
	}

	private static int responseCount(Response response) {
		int result = 1;
		while ((response = response.priorResponse()) != null) {
			result++;
		}
		return result;
	}

	private static String makeRequest(OkHttpClient client, RequestType type, String url, String bodyJson)
			throws IOException {
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
			throw new WebServiceException("Call failed. (" + response.code() + ")");
		}

	}

	private void performIssueTransition(String issueId, Integer transitionId) throws IOException {
		String url = configCache.getJiraUrl() + "/rest/api/2/issue/" + issueId + "/transitions";
		String json = "{\"transition\":{\"id\":\"" + transitionId + "\"}}";
		makeRequest(client, RequestType.POST, url, json);
	}
}
