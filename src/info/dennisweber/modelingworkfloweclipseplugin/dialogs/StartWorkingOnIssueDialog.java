package info.dennisweber.modelingworkfloweclipseplugin.dialogs;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Shell;

import info.dennisweber.modelingworkfloweclipseplugin.model.GitInterface;
import info.dennisweber.modelingworkfloweclipseplugin.model.Issue;
import info.dennisweber.modelingworkfloweclipseplugin.model.IssueStatus;
import info.dennisweber.modelingworkfloweclipseplugin.model.WebApi;

public class StartWorkingOnIssueDialog extends TitleAreaDialog {
	private Shell parentShell;
	private GitInterface gitInterface;
	private WebApi jiraApi;
	private Issue issue;
	private Set<Button> branchButtons = new HashSet<Button>();

	public StartWorkingOnIssueDialog(Shell parentShell, Issue issue, GitInterface gitInterface, WebApi jiraApi) {
		super(parentShell);
		this.parentShell = parentShell;
		this.issue = issue;
		this.gitInterface = gitInterface;
		this.jiraApi = jiraApi;
	}

	@Override
	public void create() {
		super.create();
		setTitle("Start working on issue" + " [" + issue.getId() + "] " + issue.getTitle());
		setMessage("What release-branch is this issue relevant to?", IMessageProvider.NONE);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		GridLayout layout = new GridLayout(1, false);
		container.setLayout(layout);

		Group branchSelectionGroup = new Group(container, SWT.NONE);
		branchSelectionGroup.setLayout(new RowLayout(SWT.VERTICAL));
		branchSelectionGroup.setText("Relevant Release:");

		for (String releaseBranch : gitInterface.getReleaseBranches()) {
			Button button = new Button(branchSelectionGroup, SWT.RADIO);
			button.setText(releaseBranch);
			button.setData(releaseBranch);
			branchButtons.add(button);
		}

		Button masterBranchButton = new Button(branchSelectionGroup, SWT.RADIO);
		masterBranchButton.setText("Multiple releases or master branch");
		masterBranchButton.setData("origin/master"); // Name of the branch
		masterBranchButton.setSelection(true); // Select master by default
		branchButtons.add(masterBranchButton);

		return area;
	}

	@Override
	protected void okPressed() {
		String baseBranch = "master"; // default;
		for (Button button : branchButtons) {
			if (button.getSelection()) {
				baseBranch = button.getData().toString();
			}
		}

		String newBranchName = "issue/" + issue.getId();
		gitInterface.createBranch(baseBranch, newBranchName); // Also checks out the branch

		try {
			jiraApi.moveIssueInProgress(issue.getId());
			issue.setStatus(IssueStatus.InProgress);
		} catch (IOException e) {
			MessageDialog.openError(parentShell, "Failed to update Jira Issue", e.getLocalizedMessage());
			e.printStackTrace();
		}

		super.okPressed();
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	@Override
	public boolean isHelpAvailable() {
		return false;
	}

}
