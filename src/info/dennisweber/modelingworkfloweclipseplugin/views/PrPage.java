package info.dennisweber.modelingworkfloweclipseplugin.views;

import java.io.IOException;
import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
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

import info.dennisweber.modelingworkfloweclipseplugin.model.ConfigCache;
import info.dennisweber.modelingworkfloweclipseplugin.model.GitInterface;
import info.dennisweber.modelingworkfloweclipseplugin.model.Issue;
import info.dennisweber.modelingworkfloweclipseplugin.model.PrDto;
import info.dennisweber.modelingworkfloweclipseplugin.model.WebApi;

public class PrPage extends SubPage {
	private Issue issue;
	private PrDto pr;

	public PrPage(Composite originalParent, WebApi jiraApi, ConfigCache configCache, Shell shell,
			GitInterface gitInterface, MainView mainView, Issue issue, PrDto pr) {

		super(originalParent, jiraApi, configCache, shell, gitInterface, mainView);
		this.issue = issue;
		this.pr = pr;

		// Layout this Page
		parent = new Composite(originalParent, SWT.NONE);
		parent.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		GridLayout mainLayout = new GridLayout();
		mainLayout.numColumns = 1;
		parent.setLayout(mainLayout);

		boolean canBeMerged = false;
		try {
			canBeMerged = webApi.canMergePr(pr.id);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		initTopLabels();
		initIssueStatusRadioButtons();
		initChangedRessources();
		initConflictLabels(canBeMerged);
		initAcceptButton(canBeMerged);
		initDeclineButton();
		initCancelButton();
	}

	private void initAcceptButton(boolean canBeMerged) {
		Button btn = new Button(parent, SWT.NONE);
		btn.setText("Accept pull request and merge changes");
		btn.addListener(SWT.Selection, e -> {
			// TODO: Implement acceptance of PR
		});
		if (!canBeMerged) {
			btn.setEnabled(false);
		}
	}

	private void initDeclineButton() {
		Button btn = new Button(parent, SWT.NONE);
		btn.setText("Decline pull request");
		btn.addListener(SWT.Selection, e -> {
			MessageDialog.openError(shell, "Not impelemtend in protype",
					"This featuer is not available in the prototype. It can still be done via the Bitbucket Web UI");
		});

	}

	private void initConflictLabels(boolean canBeMerged) {
		Label lbl = new Label(parent, SWT.None);
		if (canBeMerged) {
			lbl.setText("Can be merged.");
			lbl.setForeground(shell.getDisplay().getSystemColor(SWT.COLOR_DARK_GREEN));
		} else {
			lbl.setText(
					"Conflicts with base branch! Fix the conflicts in " + pr.toRef.displayId + " to be able to merge.");
			lbl.setForeground(shell.getDisplay().getSystemColor(SWT.COLOR_RED));
		}

	}

	private void initCancelButton() {
		Button btn = new Button(parent, SWT.NONE);
		btn.setText("Cancel");
		btn.addListener(SWT.Selection, e -> {
			gitInterface.checkout("master");
			mainView.showMasterPage();
		});
	}

	private void initChangedRessources() {
		// Changed resources:
		new Label(parent, SWT.NONE).setText("Changed Resources in this PR:");
		Table changesTable = new Table(parent, SWT.BORDER);
		changesTable.setLinesVisible(true);
		changesTable.setHeaderVisible(true);
		changesTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		new TableColumn(changesTable, SWT.NONE).setText("Ressource");
		changesTable.getColumn(0).pack();

		String thisBranch = pr.fromRef.displayId;
		String mergeOntoBranch = pr.toRef.displayId;
		List<String> changedFiles = gitInterface.getChangedFilesBetweenTwoBranches(mergeOntoBranch, thisBranch);
		for (String fileName : changedFiles) {
			TableItem item = new TableItem(changesTable, SWT.NONE);
			item.setText(0, fileName);
		}
		changesTable.getColumn(0).pack();

	}

	private void initIssueStatusRadioButtons() {
		Group issueStatusGroup = new Group(parent, SWT.NONE);
		issueStatusGroup.setLayout(new RowLayout(SWT.VERTICAL));
		issueStatusGroup.setText("Status of issue [" + issue.getId() + "] " + issue.getTitle());
		Button issueStatusBtnInReview = new Button(issueStatusGroup, SWT.RADIO);
		issueStatusBtnInReview.setText("In Review");
		Button issueStatusBtnInProgress = new Button(issueStatusGroup, SWT.RADIO);
		issueStatusBtnInProgress.setText("In Progress");

		SelectionListener selectionListener = new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				try {
					if (issueStatusBtnInProgress.getSelection()) {
						// User switched to "In Progress"
						webApi.moveIssueReopen(issue.getId());
						webApi.moveIssueInProgress(issue.getId());
					} else {
						// User switched to "In Review"
						webApi.moveIssueInReview(issue.getId());
					}
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
			};
		};
		issueStatusBtnInProgress.addSelectionListener(selectionListener);

		// Set default value
		switch (issue.getStatus()) {
		case InProgress:
			issueStatusBtnInProgress.setSelection(true);
			break;
		case InReview:
			issueStatusBtnInReview.setSelection(true);
			break;
		default:
			throw new RuntimeException("Issue " + issue.getId() + " is in status " + issue.getStatus()
					+ ". This should not happen when viewing the Pull Request");
		}
	}

	private void initTopLabels() {
		Link link = new Link(parent, SWT.NONE);
		link.setText("Viewing Pull Request <a>[" + pr.id + "]</a> to merge changes from <a>" + pr.fromRef.displayId
				+ "</a> into <a>" + pr.toRef.displayId + "</a>");
		link.addListener(SWT.Selection, event -> {
			Program.launch(configCache.getBbBaseUrl() + configCache.getBbRepoPath() + "/pull-requests/" + pr.id);
		});
	}
}
