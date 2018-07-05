package info.dennisweber.modelingworkfloweclipseplugin.model;

import java.io.IOException;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import javax.net.ssl.SSLHandshakeException;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.reflect.TypeToken;

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
	private Gson gson = new Gson();

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

	public Set<Issue> getIssues() {
		try {
			String url = configCache.getJiraApiUrl() + "sprint" + "/" + getActiveSprint() + "/" + "issue";
			Request request = new Request.Builder().url(url).build();
			Response response = client.newCall(request).execute();
			String json = response.body().string();

			// System.out.println(response.body().string());
			// TODO: Request ONLY THE ACTIVE Issues and return them
		} catch (SSLHandshakeException e) {
			System.out.println("SSL ERROR!");
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		return null;
	}

	private int responseCount(Response response) {
		int result = 1;
		while ((response = response.priorResponse()) != null) {
			result++;
		}
		return result;
	}
}
