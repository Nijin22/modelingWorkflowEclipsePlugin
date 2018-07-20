package info.dennisweber.modelingworkfloweclipseplugin.dialogs;

import java.io.IOException;
import java.util.List;

import org.eclipse.jface.dialogs.IMessageProvider;
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
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.TableItem;

import info.dennisweber.modelingworkfloweclipseplugin.model.GitInterface;
import info.dennisweber.modelingworkfloweclipseplugin.model.Issue;
import info.dennisweber.modelingworkfloweclipseplugin.model.IssueStatus;
import info.dennisweber.modelingworkfloweclipseplugin.model.PrDto;
import info.dennisweber.modelingworkfloweclipseplugin.model.WebApi;

public class CreatePrDialog extends TitleAreaDialog {
	private Shell shell; // TODO: Replace in all files with super.getShell
	private Issue issue;
	private GitInterface gitInterface;
	private WebApi webApi;

	private String basedOnBranch;

	private Table changesTable;
	private Button issueStatusBtnYes;
	private PrDto createdPr = null;

	public CreatePrDialog(Shell parentShell, Issue issue, GitInterface gitInterface, WebApi webApi) {
		super(parentShell);
		this.shell = parentShell;
		this.issue = issue;
		this.gitInterface = gitInterface;
		this.webApi = webApi;

		this.basedOnBranch = gitInterface.getBasedOnBranch();
	}

	@Override
	public void create() {
		super.create();
		setTitle("Creating a Pull Request for Issue " + " [" + issue.getId() + "] " + issue.getTitle());
		setMessage("Merging issue/" + issue.getId() + " back into " + this.basedOnBranch, IMessageProvider.NONE);
	}

	/**
	 * Return the created Pull Request. Might be null if the dialog did not create a
	 * PR.
	 * 
	 * @return The create PR.
	 */
	public PrDto getCreatedPr() {
		return createdPr;
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		GridLayout layout = new GridLayout(1, false);
		container.setLayout(layout);

		// Issue status selection:
		Group issueStatusGroup = new Group(container, SWT.NONE);
		issueStatusGroup.setLayout(new RowLayout(SWT.VERTICAL));
		issueStatusGroup.setText("Set issue to 'in review'?");
		issueStatusBtnYes = new Button(issueStatusGroup, SWT.RADIO);
		issueStatusBtnYes.setText("Yes, let someone review this PR.");
		Button issueStatusBtnNo = new Button(issueStatusGroup, SWT.RADIO);
		issueStatusBtnNo.setText("No, just create the PR but keep the issue 'in progress'.");
		issueStatusBtnNo.setSelection(true); // Default choice

		// Changed resources:
		new Label(container, SWT.NONE).setText("Changed Resources in this PR");
		changesTable = new Table(container, SWT.BORDER);
		changesTable.setLinesVisible(true);
		changesTable.setHeaderVisible(true);
		changesTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		new TableColumn(changesTable, SWT.NONE).setText("Ressource");
		changesTable.getColumn(0).pack();

		reloadChanges();

		return area;
	}

	@Override
	protected void okPressed() {
		// Create PR:
		try {
			createdPr = webApi.createPr(gitInterface.getCurrentBranch(), basedOnBranch);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		System.out.println("PR created.");

		// Set the issue to "In review" if that's what the user selected.
		if (issueStatusBtnYes.getSelection()) {
			// User selected that someone should review this issue.
			try {
				webApi.moveIssueInReview(issue.getId());
				issue.setStatus(IssueStatus.InReview);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
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

	private void reloadChanges() {
		String thisBranch = "HEAD";

		List<String> changedFiles = gitInterface.getChangedFilesBetweenTwoBranches(basedOnBranch, thisBranch);

		changesTable.removeAll();
		for (String fileName : changedFiles) {
			TableItem item = new TableItem(changesTable, SWT.NONE);
			item.setText(0, fileName);
		}

		changesTable.getColumn(0).pack();

	}

}
