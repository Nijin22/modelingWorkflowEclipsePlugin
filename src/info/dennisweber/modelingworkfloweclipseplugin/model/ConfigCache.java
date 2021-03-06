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

	public void update(String bitbucketBaseUrl, String bbRepoPath, String jiraApiUrl, String jiraBoardId,
			String username, String password) {
		dto.bitbucketBaseUrl = bitbucketBaseUrl;
		dto.bitbucketRepoPath = bbRepoPath;
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
			dto.username = "svc.denweb-thesis";
		}
		return dto.username;
	}

	public String getPassword() {
		if (dto.password == null) {
			dto.password = "";
		}
		return dto.password;
	}

	public String getBbBaseUrl() {
		// Set defaults for prototype
		if (dto.bitbucketBaseUrl == null) {
			dto.bitbucketBaseUrl = "https://git.actico.com";
		}

		return dto.bitbucketBaseUrl;
	}

	public String getBbRepoPath() {
		// Set defaults for prototype
		if (dto.bitbucketRepoPath == null) {
			dto.bitbucketRepoPath = "/projects/DEN/repos/masterthesis-example-project";
		}

		return dto.bitbucketRepoPath;
	}

	public String getJiraUrl() {
		// Set defaults for prototype
		if (dto.jiraUrl == null) {
			dto.jiraUrl = "https://issues.actico.com";
		}

		return dto.jiraUrl;
	}

	public String getJiraBoardId() {
		// Defaults for prototype
		if (dto.jiraBoardId == null) {
			dto.jiraBoardId = "13";
		}

		return dto.jiraBoardId;
	}

	public boolean isConfigured() {
		return isConfigured;
	}

}
