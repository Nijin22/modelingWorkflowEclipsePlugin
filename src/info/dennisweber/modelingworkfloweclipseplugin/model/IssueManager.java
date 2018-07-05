package info.dennisweber.modelingworkfloweclipseplugin.model;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.net.ssl.SSLHandshakeException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

import info.dennisweber.modelingworkfloweclipseplugin.ConfigCache;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class IssueManager {
	private static Set<Issue> issues = new HashSet<Issue>();
	private static OkHttpClient client = new OkHttpClient();

	public static void fillSwtTable(Table table) {
		updateIssues();

		// Draw the update
		Display.getDefault().syncExec(() -> {
			int count = 12;
			for (int i = 0; i < count; i++) {
				TableItem item = new TableItem(table, SWT.NONE);
				item.setText(0, Integer.toString(i));
				item.setText(1, "Foo the bar");
				item.setText(2, "ToDo");
				item.setText(3, "John Doe");
				item.setText(4, "This should be clickable");
			}

			// Adjust the width of columns
			for (int i = 0; i < table.getColumnCount(); i++) {
				table.getColumn(i).pack();
			}
		});
	}

	private static void updateIssues() {
		try {
			String url = ConfigCache.getJiraApiUrl() + "issue";
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

	}
}
