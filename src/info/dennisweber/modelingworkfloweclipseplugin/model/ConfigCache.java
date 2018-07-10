package info.dennisweber.modelingworkfloweclipseplugin.model;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import org.eclipse.core.resources.IProject;

import com.google.gson.Gson;
import com.google.gson.stream.JsonReader;

public class ConfigCache {
	Gson gson = new Gson();
	private ConfigDto dto;
	private boolean isConfigured = false;
	private String configFilePath;

	public ConfigCache(IProject project) {
		configFilePath = project.getLocation() + "/.git/modellingWorkflowConfig.json";

		// Try to load config from configuration file
		File f = new File(configFilePath);
		if (f.isFile()) {
			try {
				JsonReader reader = new JsonReader(new FileReader(configFilePath));
				dto = gson.fromJson(reader, ConfigDto.class);
				System.out.println("Configuration read from file.");
				isConfigured = true;
			} catch (FileNotFoundException e) {
				// Should never happens, as we check for file existence before.
				throw new RuntimeException(e);
			}
		} else {
			System.out.println("Initializing new configuration...");
			dto = new ConfigDto();
		}
	}

	public void update(String repoApiUrl, String jiraApiUrl, String jiraBoardId, String username, String password) {
		// TODO: Verify entered data is valid
		dto.repoApiUrl = repoApiUrl;
		dto.jiraUrl = jiraApiUrl;
		dto.jiraBoardId = jiraBoardId;
		dto.username = username;
		dto.password = password;

		isConfigured = true;
	}

	public void storeConfig() throws IOException {
		String json = gson.toJson(dto, ConfigDto.class);
		File f = new File(configFilePath);

		FileWriter fw = new FileWriter(f, false);
		fw.write(json);
		System.out.println("Configuration saved to: " + configFilePath);
		fw.close();
	}

	public String getUsername() {
		if (dto.username == null) {
			dto.username = "denweb01";
		}
		return dto.username;
	}

	public String getPassword() {
		if (dto.password == null) {
			dto.password = "";
		}
		return dto.password;
	}

	public String getRepoApiUrl() {
		// Set defaults for prototype
		if (dto.repoApiUrl == null) {
			dto.repoApiUrl = "https://stash.bfs-intra.net/rest/api/1.0/users/denweb01/repos/masterthesis-example-model";
		}

		return dto.repoApiUrl;
	}

	public String getJiraUrl() {
		// Set defaults for prototype
		if (dto.jiraUrl == null) {
			dto.jiraUrl = "https://jira.bfs-intra.net";
		}

		return dto.jiraUrl;
	}

	public String getJiraBoardId() {
		// Defaults for prototype
		if (dto.jiraBoardId == null) {
			dto.jiraBoardId = "137";
		}

		return dto.jiraBoardId;
	}

	public boolean isConfigured() {
		return isConfigured;
	}

}
