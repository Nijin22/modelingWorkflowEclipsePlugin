package info.dennisweber.modelingworkfloweclipseplugin.model;

import java.util.HashSet;
import java.util.Set;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;

public class IssueManager {
	private static Set<Issue> issues = new HashSet<Issue>();

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
				
	}
}
