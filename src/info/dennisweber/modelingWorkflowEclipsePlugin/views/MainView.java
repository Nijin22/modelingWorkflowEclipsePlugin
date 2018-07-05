package info.dennisweber.modelingworkfloweclipseplugin.views;

import java.time.Instant;

import org.eclipse.jface.dialogs.MessageDialog;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.part.ViewPart;

import info.dennisweber.modelingworkfloweclipseplugin.dialogs.ConfigurationDialog;
import info.dennisweber.modelingworkfloweclipseplugin.model.IssueManager;

public class MainView extends ViewPart {

	public MainView() {
		super();
	}

	public void setFocus() {
	}

	public void createPartControl(Composite parent) {
		GridLayout mainLayout = new GridLayout();
		mainLayout.numColumns = 1;
		parent.setLayout(mainLayout);

		// Issues:
		Group issueGroup = initIssueGroup(parent);
		Table issueTable = initIssueTable(issueGroup);
		Link issueLink = initCreateIssueLink(issueGroup);
		Link viewAllIssuesLink = initViewAllIssuesLink(issueGroup);

		// Release-Branches:
		Group releaseBranchesGroup = initReleaseBranchesGroup(parent);
		Table releaseBranchesTable = initReleaseBranchesTable(releaseBranchesGroup);
		Button createNewReleaseBranchButton = initCreateNewReleaseBranchButton(releaseBranchesGroup);
		Button buildReleaseFromMasterButton = initBuildReleaseFromMasterButton(releaseBranchesGroup);

		// Config Button:
		initConfigButton(parent);

		System.out.println(Instant.now().toString() + " Loaded.");
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
		Table issueTable = new Table(parent, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);
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
			IssueManager.fillSwtTable(issueTable);
		}).start();
		;

		return issueTable;
	}

	private Link initViewAllIssuesLink(Group issueGroup) {
		Link viewAllIssuesLink = new Link(issueGroup, SWT.NONE);
		viewAllIssuesLink.setText("<a>View all issues on Bitbucket</a>");
		viewAllIssuesLink.addListener(SWT.Selection, event -> {
			Program.launch("https://example.org"); // Launch the default browser
			// TODO: This should link somewhere more useful.
		});

		return viewAllIssuesLink;
	}

	private Link initCreateIssueLink(Group issueGroup) {
		Link createIssueLink = new Link(issueGroup, SWT.NONE);
		createIssueLink.setText("<a>Create new issue</a>");
		createIssueLink.addListener(SWT.Selection, event -> {
			Program.launch("https://example.org"); // Launch the default browser
			// TODO: This should link somewhere more useful.
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
		Table releaseBranchesTable = new Table(parent, SWT.MULTI | SWT.BORDER | SWT.FULL_SELECTION);
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
			Shell shell = this.getSite().getWorkbenchWindow().getShell();
			MessageDialog.openError(shell, "Not implemented", "This feature is not implemented yet.");
			// TODO: Make the button do something useful
		});

		return button;
	}

	private Button initBuildReleaseFromMasterButton(Composite parent) {
		Button button = new Button(parent, SWT.PUSH);
		button.setText("Build release from master");
		button.addListener(SWT.Selection, event -> {
			Shell shell = this.getSite().getWorkbenchWindow().getShell();
			MessageDialog.openError(shell, "Not implemented", "This feature is not implemented yet.");
			// TODO: Make the button do something useful
		});

		return button;
	}

	private Button initConfigButton(Composite parent) {
		Button button = new Button(parent, SWT.PUSH);
		button.setText("Configuration");
		button.addListener(SWT.Selection, event -> {
			Shell shell = this.getSite().getWorkbenchWindow().getShell();
			ConfigurationDialog configDialog = new ConfigurationDialog(shell);
			configDialog.create();
			configDialog.open(); // Open dialog and block until it is closed again
		});

		return button;
	}
}
