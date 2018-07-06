package info.dennisweber.modelingworkfloweclipseplugin.dialogs;

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
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import info.dennisweber.modelingworkfloweclipseplugin.model.Issue;

public class StartWorkingOnIssueDialog extends TitleAreaDialog {
	private Shell parentShell;
	private Issue issue;

	public StartWorkingOnIssueDialog(Shell parentShell, Issue issue) {
		super(parentShell);
		this.parentShell = parentShell;
		this.issue = issue;
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
		branchSelectionGroup.setText("Relevant Release:" );
		
		// TODO: Loop release-Branches
		
		Button masterBranchButton = new Button(branchSelectionGroup, SWT.RADIO);
		masterBranchButton.setText("Multiple releases or master branch");
		masterBranchButton.setSelection(true); // Select master by default
		
		return area;
	}

	@Override
	protected void okPressed() {
		// TODO: Implement
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
