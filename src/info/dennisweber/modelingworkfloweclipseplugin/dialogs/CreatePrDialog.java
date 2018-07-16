package info.dennisweber.modelingworkfloweclipseplugin.dialogs;

import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.TableEditor;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
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
import info.dennisweber.modelingworkfloweclipseplugin.model.WebApi;

public class CreatePrDialog extends TitleAreaDialog {
	private Shell shell;
	private Issue issue;
	private GitInterface gitInterface;
	private WebApi webApi;

	private Set<Button> branchButtons = new HashSet<Button>();
	private Table changesTable;

	public CreatePrDialog(Shell parentShell, Issue issue, GitInterface gitInterface, WebApi webApi) {
		super(parentShell);
		this.shell = parentShell;
		this.issue = issue;
		this.gitInterface = gitInterface;
		this.webApi = webApi;
	}

	@Override
	public void create() {
		super.create();
		setTitle("Creating a Pull Request for Issue " + " [" + issue.getId() + "] " + issue.getTitle());
		// setMessage("MESSAGE HERE", IMessageProvider.NONE);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		GridLayout layout = new GridLayout(1, false);
		container.setLayout(layout);

		// Branch selection:
		SelectionListener listener = new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (((Button) e.widget).getSelection()) {
					reloadChanges();
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			}
		};
		Group branchSelectionGroup = new Group(container, SWT.NONE);
		branchSelectionGroup.setLayout(new RowLayout(SWT.VERTICAL));
		branchSelectionGroup.setText("Branch to merge into:");
		for (String releaseBranch : gitInterface.getReleaseBranches()) {
			releaseBranch = releaseBranch.replace("origin/", "");
			Button button = new Button(branchSelectionGroup, SWT.RADIO);
			button.setText(releaseBranch);
			button.setData(releaseBranch);
			button.addSelectionListener(listener);
			branchButtons.add(button);
		}
		Button masterBranchButton = new Button(branchSelectionGroup, SWT.RADIO);
		masterBranchButton.setText("master");
		masterBranchButton.setData("master"); // Name of the branch
		masterBranchButton.setSelection(true); // Select master by default
		masterBranchButton.addSelectionListener(listener);
		branchButtons.add(masterBranchButton);

		// Issue status selection:
		Group issueStatusGroup = new Group(container, SWT.NONE);
		issueStatusGroup.setLayout(new RowLayout(SWT.VERTICAL));
		issueStatusGroup.setText("Set issue to 'in review'?");
		Button issueStatusBtnYes = new Button(issueStatusGroup, SWT.RADIO);
		issueStatusBtnYes.setText("Yes, let someone review this PR.");
		issueStatusBtnYes.setData(true);
		Button issueStatusBtnNo = new Button(issueStatusGroup, SWT.RADIO);
		issueStatusBtnNo.setText("No, just create the PR but keep the issue 'in progress'.");
		issueStatusBtnNo.setData(false);
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
			webApi.createPr(gitInterface.getCurrentBranch(), getSelectedBranch());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}

		// TODO: Set the issue to "In review" if that's the case
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
		String mergeOntoBranch = getSelectedBranch();

		List<String> changedFiles = gitInterface.getChangedFilesBetweenTwoBranches(mergeOntoBranch, thisBranch);

		changesTable.removeAll();
		for (String fileName : changedFiles) {
			TableItem item = new TableItem(changesTable, SWT.NONE);
			item.setText(0, fileName);
		}

		changesTable.getColumn(0).pack();

	}

	private String getSelectedBranch() {
		for (Button button : branchButtons) {
			if (button.getSelection()) {
				return button.getData().toString();
			}
		}

		throw new RuntimeException("No button selected."); // Should never happen
	}

}
