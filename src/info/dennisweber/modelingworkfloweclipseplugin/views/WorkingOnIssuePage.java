package info.dennisweber.modelingworkfloweclipseplugin.views;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.program.Program;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Link;
import org.eclipse.swt.widgets.Shell;

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

	private Link currentIssueLabel;
	private Button backToOverviewButton;

	public WorkingOnIssuePage(Composite parent, JiraRestApi jiraApi, ConfigCache configCache, Shell shell,
			GitInterface gitInterface, MainView mainView, Issue issue) {
		this.jiraApi = jiraApi;
		this.gitInterface = gitInterface;
		this.shell = shell;
		this.parent = parent;
		this.configCache = configCache;
		this.mainView = mainView;
		this.issue = issue;
	}

	public void init() {
		parent.setLayout(new GridLayout(2, true));

		// Init headers
		initCurrentIssueLabel();

		initBackToOverviewButton();

		// Redraw the layout
		parent.layout(true);
	}

	public void dispose() {
		currentIssueLabel.dispose();
		backToOverviewButton.dispose();

		// Redraw the layout
		parent.layout(true);
	}

	private void initCurrentIssueLabel() {
		currentIssueLabel = new Link(parent, SWT.NONE);
		currentIssueLabel.setText("Current Issue: <a>[" + issue.getId() + "] " + issue.getTitle() + "</a>");
		currentIssueLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		currentIssueLabel.addListener(SWT.Selection, event -> {
			Program.launch(configCache.getJiraUrl() + "/browse/" + issue.getId());
		});
	}

	private void initBackToOverviewButton() {
		backToOverviewButton = new Button(parent, SWT.NONE);
		backToOverviewButton.setText("Back to Overview");
		backToOverviewButton.addListener(SWT.Selection, event -> {
			// TODO: What to do with uncommited changes? Stash? Commit as a "WIP"?
			gitInterface.checkout("master");
			mainView.showMasterPage();
		});
	}

}
