package info.dennisweber.modelingworkfloweclipseplugin.model;

import java.io.IOException;
import java.util.Set;

import javax.net.ssl.SSLHandshakeException;

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

	public Set<Issue> getIssues() {
		try {
			String url = configCache.getJiraApiUrl() + "issue";
			Request request = new Request.Builder().url(url).build();
			Response response = client.newCall(request).execute();

			System.out.println(response.body().string());
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
