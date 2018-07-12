package info.dennisweber.modelingworkfloweclipseplugin.views;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.swt.widgets.Text;

import info.dennisweber.modelingworkfloweclipseplugin.model.CommitDto;
import info.dennisweber.modelingworkfloweclipseplugin.model.ConfigCache;
import info.dennisweber.modelingworkfloweclipseplugin.model.GitInterface;
import info.dennisweber.modelingworkfloweclipseplugin.model.Issue;
import info.dennisweber.modelingworkfloweclipseplugin.model.JiraRestApi;

public class WorkingOnIssuePage {
	private JiraRestApi jiraApi;
	private GitInterface gitInterface;
	private Shell shell;
	private Composite parent;
	private ConfigCache configCache;
	private MainView mainView;
	private Issue issue;

	private Text commitMessageTextbox;
	private Table newChangesTable;
	private Table indexTable;
	private Table logTable;

	private final String buttonKey = "SWT-BUTTON"; // Used to keep track of created SWT Buttons for later disposal

	public WorkingOnIssuePage(Composite originalParent, JiraRestApi jiraApi, ConfigCache configCache, Shell shell,
			GitInterface gitInterface, MainView mainView, Issue issue) {
		this.jiraApi = jiraApi;
		this.gitInterface = gitInterface;
		this.shell = shell;
		this.configCache = configCache;
		this.mainView = mainView;
		this.issue = issue;

		// Layout this Page
		parent = new Composite(originalParent, SWT.NONE);
		parent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		GridLayout mainLayout = new GridLayout();
		mainLayout.numColumns = 2;
		parent.setLayout(mainLayout);

		// Init widgets
		initCurrentIssueLabel();
		initRefreshButton();
		initNewChanges();
		initChangesLog();
		initBackToOverviewButton();

		refreshTables();
	}

	public void dispose() {
		parent.dispose();
	}

	private void initCurrentIssueLabel() {
		Link currentIssueLabel = new Link(parent, SWT.NONE);
		currentIssueLabel.setText("Current Issue: <a>[" + issue.getId() + "] " + issue.getTitle() + "</a>");
		currentIssueLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 2, 1));
		currentIssueLabel.addListener(SWT.Selection, event -> {
			Program.launch(configCache.getJiraUrl() + "/browse/" + issue.getId());
		});
	}

	private void initRefreshButton() {
		Button refreshButton = new Button(parent, SWT.NONE);
		refreshButton.setText("Refresh / Check for changes");
		refreshButton.addListener(SWT.Selection, event -> {
			refreshTables();
		});
		refreshButton.setLayoutData(new GridData(SWT.NONE, SWT.NONE, false, false, 2, 1));
		refreshButton.pack();
	}

	private void initNewChanges() {
		// Create group
		Group newChangesGroup = new Group(parent, SWT.NONE);
		newChangesGroup.setText("New changes:");
		newChangesGroup.setLayout(new GridLayout(1, true));
		newChangesGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		// "New changes" label
		Label newChangesLabel = new Label(newChangesGroup, SWT.NONE);
		newChangesLabel.setText("New changes:");

		// "New changes" table
		newChangesTable = new Table(newChangesGroup, SWT.BORDER);
		newChangesTable.setLinesVisible(true);
		newChangesTable.setHeaderVisible(true);
		newChangesTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		// "Add all to index" button
		Button addAllToIndexBtn = new Button(newChangesGroup, SWT.NONE);
		addAllToIndexBtn.setText("Add all ressources to index");
		addAllToIndexBtn.addListener(SWT.Selection, e -> {
			gitInterface.addAll();
			refreshTables();
		});

		// "Index" label
		Label indexLabel = new Label(newChangesGroup, SWT.NONE);
		indexLabel.setText("Index:");

		// "Index table"
		indexTable = new Table(newChangesGroup, SWT.BORDER);
		indexTable.setLinesVisible(true);
		indexTable.setHeaderVisible(true);
		indexTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		// "Remove all from index" button
		Button removeAllFromIndexBtn = new Button(newChangesGroup, SWT.NONE);
		removeAllFromIndexBtn.setText("Remove all resources from index");
		removeAllFromIndexBtn.addListener(SWT.Selection, e -> {
			gitInterface.unstageAll();
			refreshTables();
		});

		// Columns for both tables
		String[] titles = { "Ressource", "Action" };
		for (int i = 0; i < titles.length; i++) {
			new TableColumn(newChangesTable, SWT.NONE).setText(titles[i]);
			new TableColumn(indexTable, SWT.NONE).setText(titles[i]);
		}
		new TableItem(newChangesTable, SWT.NONE).setText(0, "Table data not initialized yet.");
		new TableItem(indexTable, SWT.NONE).setText(0, "Table data not initialized yet.");
		for (int i = 0; i < titles.length; i++) {
			newChangesTable.getColumn(i).pack();
			indexTable.getColumn(i).pack();
		}

		// "Persist changes" label
		Label commitLabel = new Label(newChangesGroup, SWT.NONE);
		commitLabel.setText("Persist changes:");

		// Commit message box
		commitMessageTextbox = new Text(newChangesGroup, SWT.MULTI | SWT.BORDER | SWT.SEARCH);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false);
		gridData.heightHint = 3 * commitMessageTextbox.getLineHeight();
		commitMessageTextbox.setLayoutData(gridData);
		commitMessageTextbox.setMessage("Describe your changes here.");

		// Commit + Sync Button
		Button commitButton = new Button(newChangesGroup, SWT.NONE);
		commitButton.setText("Save and sync changes");
		commitButton.setEnabled(false);
		commitButton.addListener(SWT.Selection, event -> {
			// TODO: Implement
			MessageDialog.openError(shell, "Not implemented yet", "not implemented yet.");
		});
	}

	private void initChangesLog() {
		Group changesLogGroup = new Group(parent, SWT.NONE);
		changesLogGroup.setText("Changes of this issue:");
		changesLogGroup.setLayout(new GridLayout(1, true));
		changesLogGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		// "Index table"
		logTable = new Table(changesLogGroup, SWT.BORDER);
		logTable.setLinesVisible(true);
		logTable.setHeaderVisible(true);
		logTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		String[] titles = { "Time", "Message", "Action" };
		for (int i = 0; i < titles.length; i++) {
			new TableColumn(logTable, SWT.NONE).setText(titles[i]);
		}
		new TableItem(logTable, SWT.NONE).setText(0, "Table data not initialized yet.");
		for (int i = 0; i < titles.length; i++) {
			logTable.getColumn(i).pack();
		}

		// Create PR Button
		Button createPrButton = new Button(changesLogGroup, SWT.NONE);
		createPrButton.setText("Create Pull Request");
		createPrButton.addListener(SWT.Selection, event -> {
			Program.launch(configCache.getBbBaseUrl() + configCache.getBbRepoPath() + "pull-requests?create");
		});

	}

	private void initBackToOverviewButton() {
		Button backToOverviewButton = new Button(parent, SWT.NONE);
		backToOverviewButton.setText("Back to Overview");
		backToOverviewButton.addListener(SWT.Selection, event -> {
			// TODO: What to do with uncommited changes? Stash? Commit as a "WIP"?
			gitInterface.checkout("master");
			mainView.showMasterPage();
		});
	}

	private void refreshTables() {

		// Current Changes
		emptyTable(newChangesTable);
		for (String fileName : gitInterface.getUntrackedFiles()) {
			TableItem item = new TableItem(newChangesTable, SWT.NONE);
			item.setText(0, fileName);
			
			// Action Button
			TableEditor editor = new TableEditor(newChangesTable);
			Button button = new Button(newChangesTable, SWT.PUSH);
			button.setText("Add to Index");
			button.addListener(SWT.Selection, event -> {
				MessageDialog.openError(shell, "Not implemented yet", "not implemented yet");
				// TODO: Implement
			});
			button.pack();
			editor.minimumWidth = button.getSize().x;
			editor.horizontalAlignment = SWT.LEFT;
			editor.setEditor(button, item, 1);
			item.setData(buttonKey, button);
		}

		// Index
		emptyTable(indexTable);
		for (String fileName : gitInterface.getIndexedFiles()) {
			TableItem item = new TableItem(indexTable, SWT.NONE);
			item.setText(0, fileName);
			
			// Action Button
			TableEditor editor = new TableEditor(indexTable);
			Button button = new Button(indexTable, SWT.PUSH);
			button.setText("Remove from Index");
			button.addListener(SWT.Selection, event -> {
				MessageDialog.openError(shell, "Not implemented yet", "not implemented yet");
				// TODO: Implement
			});
			button.pack();
			editor.minimumWidth = button.getSize().x;
			editor.horizontalAlignment = SWT.LEFT;
			editor.setEditor(button, item, 1);
			item.setData(buttonKey, button);
		}

		// Log
		emptyTable(logTable);
		for (CommitDto commit : gitInterface.getLog()) {
			TableItem item = new TableItem(logTable, SWT.NONE);
			item.setText(0, commit.relativeTime);
			item.setText(1, commit.message);

			// Action Button
			TableEditor editor = new TableEditor(logTable);
			Button button = new Button(logTable, SWT.PUSH);
			button.setText("Revert to commit");
			button.addListener(SWT.Selection, event -> {
				MessageDialog.openError(shell, "Not implemented yet", "not implemented yet");
				// TODO: Implement
			});
			button.pack();
			editor.minimumWidth = button.getSize().x;
			editor.horizontalAlignment = SWT.LEFT;
			editor.setEditor(button, item, 2);
			item.setData(buttonKey, button);
		}
		for (int i = 0; i < logTable.getColumnCount(); i++) {
			logTable.getColumn(i).pack();
		}

		// TODO: If index is filled and commit message is filled, enable commit button
	}

	/**
	 * Empties a table, including the added Buttons. (This assumes buttons are added
	 * as "setData" with the key "BUTTON_KEY")
	 * 
	 * @param table
	 *            The table to empty.
	 */
	private void emptyTable(Table table) {
		for (TableItem item : table.getItems()) { // Dispose the buttons we created
			if (item.getData(buttonKey) != null) {
				((Button) item.getData(buttonKey)).dispose();
			}
		}
		table.removeAll();
	}

}
