package info.dennisweber.modelingworkfloweclipseplugin.views;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import info.dennisweber.modelingworkfloweclipseplugin.dialogs.ConfigurationDialog;
import info.dennisweber.modelingworkfloweclipseplugin.dialogs.StartWorkingOnIssueDialog;
import info.dennisweber.modelingworkfloweclipseplugin.model.ConfigCache;
import info.dennisweber.modelingworkfloweclipseplugin.model.GitInterface;
import info.dennisweber.modelingworkfloweclipseplugin.model.Issue;
import info.dennisweber.modelingworkfloweclipseplugin.model.IssueStatus;
import info.dennisweber.modelingworkfloweclipseplugin.model.JiraRestApi;

public class MasterPage {
	private JiraRestApi jiraApi;
	private GitInterface gitInterface;
	private Shell shell;
	private Composite parent;
	private ConfigCache configCache;
	private MainView mainView;

	private Group releaseBranchesGroup;
	private Table issueTable;
	private Set<Button> issueTableButtons = new HashSet<Button>();
	private Button issueRefreshButton;
	private Link issueLink;
	private Link viewAllIssuesLink;

	private Group issueGroup;
	private Table releaseBranchesTable;
	private Button createNewReleaseBranchButton;
	private Button buildReleaseFromMasterButton;

	private Button configButton;

	public MasterPage(Composite originalParent, JiraRestApi jiraApi, ConfigCache configCache, Shell shell,
			GitInterface gitInterface, MainView mainView) {
		this.jiraApi = jiraApi;
		this.gitInterface = gitInterface;
		this.shell = shell;
		this.configCache = configCache;
		this.mainView = mainView;

		// Layout this Page
		this.parent = new Composite(originalParent, SWT.NONE);
		this.parent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		GridLayout mainLayout = new GridLayout();
		mainLayout.numColumns = 1;
		parent.setLayout(mainLayout);

		// Issues:
		issueGroup = initIssueGroup(parent);
		issueRefreshButton = initIssueRefreshButton(issueGroup);
		issueTable = initIssueTable(issueGroup);
		issueLink = initCreateIssueLink(issueGroup);
		viewAllIssuesLink = initViewAllIssuesLink(issueGroup);

		// Releases:
		releaseBranchesGroup = initReleaseBranchesGroup(parent);
		releaseBranchesTable = initReleaseBranchesTable(releaseBranchesGroup);
		createNewReleaseBranchButton = initCreateNewReleaseBranchButton(releaseBranchesGroup);
		buildReleaseFromMasterButton = initBuildReleaseFromMasterButton(releaseBranchesGroup);

		// Config Button:
		configButton = initConfigButton(parent);
	}

	public void dispose() {
		parent.dispose();
	}

	private Group initIssueGroup(Composite parent) {
		Group issueGroup = new Group(parent, SWT.NONE);
		issueGroup.setText("Active issues");

		// Layout:
		GridLayout gl = new GridLayout();
		gl.numColumns = 1;
		issueGroup.setLayout(gl);
		issueGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		return issueGroup;
	}

	private Table initIssueTable(Composite parent) {
		Table issueTable = new Table(parent, SWT.BORDER);
		issueTable.setLinesVisible(true);
		issueTable.setHeaderVisible(true);

		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.heightHint = 200;
		issueTable.setLayoutData(data);

		String[] titles = { "ID", "Title", "Status", "Assignee", "Action" };
		for (int i = 0; i < titles.length; i++) {
			TableColumn column = new TableColumn(issueTable, SWT.NONE);
			column.setText(titles[i]);
		}

		// Fill table in new thread
		new Thread(() -> {
			fillTable(issueTable);
		}).start();
		;

		return issueTable;
	}

	private void fillTable(Table issueTable) {
		try {
			List<Issue> issues = jiraApi.getIssues();

			// Draw the update
			Display.getDefault().syncExec(() -> {
				// Empty the table
				issueTable.removeAll();
				for (Button button : issueTableButtons) {
					button.dispose();
				}

				for (Issue issue : issues) {
					if (issue.getStatus() == IssueStatus.Done) {
						continue; // Skip the issues which are already done.
					}

					TableItem item = new TableItem(issueTable, SWT.NONE);

					item.setText(0, issue.getId());
					item.setText(1, issue.getTitle());
					item.setText(2, issue.getStatus().toString());
					item.setText(3, issue.getAssignee());

					// Action buttons:
					if (issue.getStatus() == IssueStatus.ToDo) { // Start working on issue:
						TableEditor editor = new TableEditor(issueTable);
						Button button = new Button(issueTable, SWT.PUSH);
						button.setText("Start working on issue");
						button.addListener(SWT.Selection, event -> {
							StartWorkingOnIssueDialog dialog = new StartWorkingOnIssueDialog(shell, issue, gitInterface,
									jiraApi);
							dialog.create();
							switch (dialog.open()) {// Open dialog and block until it is closed again
							case Window.OK:
								// Window closed via OK-Button
								mainView.showWorkingOnIssuePage(issue);
								break;
							default:
								// I.e. Cancel-Button
								break;
							}
						});
						button.pack();
						issueTableButtons.add(button);
						editor.minimumWidth = button.getSize().x;
						editor.horizontalAlignment = SWT.LEFT;
						editor.setEditor(button, item, 4);
					}
					if (issue.getStatus() == IssueStatus.InProgress) { // Continue working on issue:
						TableEditor editor = new TableEditor(issueTable);
						Button button = new Button(issueTable, SWT.PUSH);
						button.setText("Continue working on issue");
						button.addListener(SWT.Selection, event -> {
							gitInterface.fetch();
							gitInterface.checkout("issue/" + issue.getId());
							mainView.showWorkingOnIssuePage(issue);
						});
						button.pack();
						issueTableButtons.add(button);
						editor.minimumWidth = button.getSize().x;
						editor.horizontalAlignment = SWT.LEFT;
						editor.setEditor(button, item, 4);
					}
				}

				// Adjust the width of columns
				for (int i = 0; i < issueTable.getColumnCount(); i++) {
					issueTable.getColumn(i).pack();
				}
			});
		} catch (IOException e) {
			System.out.println("Failed to update Issues.");
			e.printStackTrace();
		}
	}

	private Button initIssueRefreshButton(Composite parent) {
		Button button = new Button(parent, SWT.PUSH);
		button.setText("Refresh issues");
		button.addListener(SWT.Selection, event -> {
			fillTable(issueTable);
		});

		return button;
	}

	private Link initViewAllIssuesLink(Group issueGroup) {
		Link viewAllIssuesLink = new Link(issueGroup, SWT.NONE);
		viewAllIssuesLink.setText("<a>View all issues on Bitbucket</a>");
		viewAllIssuesLink.addListener(SWT.Selection, event -> {
			// Launch the default browser
			Program.launch(
					configCache.getJiraUrl() + "/secure/RapidBoard.jspa?rapidView=" + configCache.getJiraBoardId());

		});

		return viewAllIssuesLink;
	}

	private Link initCreateIssueLink(Group issueGroup) {
		Link createIssueLink = new Link(issueGroup, SWT.NONE);
		createIssueLink.setText("<a>Create new issue</a>");
		createIssueLink.addListener(SWT.Selection, event -> {
			Program.launch(configCache.getJiraUrl() + "/secure/CreateIssue!default.jspa"); // Launch the default browser
		});

		return createIssueLink;
	}

	private Group initReleaseBranchesGroup(Composite parent) {
		Group releaseBranchesGroup = new Group(parent, SWT.NONE);
		releaseBranchesGroup.setText("Active release branches:");

		// Layout:
		GridLayout gl = new GridLayout();
		gl.numColumns = 1;
		releaseBranchesGroup.setLayout(gl);
		releaseBranchesGroup.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		return releaseBranchesGroup;
	}

	private Table initReleaseBranchesTable(Composite parent) {
		Table releaseBranchesTable = new Table(parent, SWT.BORDER);
		releaseBranchesTable.setLinesVisible(true);
		releaseBranchesTable.setHeaderVisible(true);

		GridData data = new GridData(SWT.FILL, SWT.FILL, true, true);
		data.heightHint = 200;
		releaseBranchesTable.setLayoutData(data);

		String[] titles = { "Release", "Last change", "Last tag", "Actions" };
		for (int i = 0; i < titles.length; i++) {
			TableColumn column = new TableColumn(releaseBranchesTable, SWT.NONE);
			column.setText(titles[i]);
		}
		int count = 2;
		for (int i = 0; i < count; i++) {
			TableItem item = new TableItem(releaseBranchesTable, SWT.NONE);
			item.setText(0, Integer.toString(i));
			item.setText(1, "Xh ago");
			item.setText(2, "3.1.4");
			item.setText(3, "This should be a button");
		}
		for (int i = 0; i < titles.length; i++) {
			releaseBranchesTable.getColumn(i).pack();
		}

		return releaseBranchesTable;
	}

	private Button initCreateNewReleaseBranchButton(Composite parent) {
		Button button = new Button(parent, SWT.PUSH);
		button.setText("Create new active release branch");
		button.addListener(SWT.Selection, event -> {
			MessageDialog.openError(shell, "Not implemented", "This feature is not implemented yet.");
			// TODO: Make the button do something useful
		});

		return button;
	}

	private Button initBuildReleaseFromMasterButton(Composite parent) {
		Button button = new Button(parent, SWT.PUSH);
		button.setText("Build release from master");
		button.addListener(SWT.Selection, event -> {
			MessageDialog.openError(shell, "Not implemented", "This feature is not implemented yet.");
			// TODO: Make the button do something useful
		});

		return button;
	}

	private Button initConfigButton(Composite parent) {
		Button button = new Button(parent, SWT.PUSH);
		button.setText("Configuration");
		button.addListener(SWT.Selection, event -> {
			ConfigurationDialog configDialog = new ConfigurationDialog(shell, configCache);
			configDialog.create();
			configDialog.open(); // Open dialog and block until it is closed again
		});

		return button;
	}
}
