package info.dennisweber.modelingworkfloweclipseplugin.views;

import java.io.IOException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.part.ViewPart;

import info.dennisweber.modelingworkfloweclipseplugin.dialogs.ConfigurationDialog;
import info.dennisweber.modelingworkfloweclipseplugin.model.ConfigCache;
import info.dennisweber.modelingworkfloweclipseplugin.model.GitInterface;
import info.dennisweber.modelingworkfloweclipseplugin.model.Issue;
import info.dennisweber.modelingworkfloweclipseplugin.model.PrDto;
import info.dennisweber.modelingworkfloweclipseplugin.model.WebApi;

public class MainView extends ViewPart {
	private ConfigCache configCache;
	private WebApi jiraApi;
	private GitInterface gitInterface;
	private IProject selectedProject = null;
	private MasterPage masterPage;
	private WorkingOnIssuePage workingOnIssuePage;
	private PrPage prPage;
	private Shell shell;
	private Composite parent;

	public MainView() {
		super();
	}

	public void setFocus() {
	}

	public void createPartControl(Composite parent) {
		shell = this.getSite().getWorkbenchWindow().getShell();
		GridLayout mainLayout = new GridLayout();
		mainLayout.numColumns = 1;
		parent.setLayout(mainLayout);
		this.parent = parent;

		initProjectSelectionCombo();
	}

	public void showMasterPage() {
		closeAllPages();

		masterPage = new MasterPage(parent, jiraApi, configCache, shell, gitInterface, this);
		parent.layout(false);
	}

	public void showWorkingOnIssuePage(Issue issue) {
		closeAllPages();

		workingOnIssuePage = new WorkingOnIssuePage(parent, jiraApi, configCache, shell, gitInterface, this, issue);
		parent.layout(false);
	}

	public void showPrPage(Issue issue, PrDto pr) {
		closeAllPages();

		prPage = new PrPage(parent, jiraApi, configCache, shell, gitInterface, this, issue, pr);
		parent.layout(false);
	}

	private void closeAllPages() {
		if (masterPage != null) {
			masterPage.dispose();
			masterPage = null;
		}
		if (workingOnIssuePage != null) {
			workingOnIssuePage.dispose();
			workingOnIssuePage = null;
		}
		if (prPage != null) {
			prPage.dispose();
			prPage = null;
		}
	}

	private void initProjectSelectionCombo() {
		Label label = new Label(parent, SWT.NONE);
		label.setText("Active project:");

		Combo combo = new Combo(parent, SWT.READ_ONLY);
		for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
			if (project.isOpen()) {
				combo.add(project.getName());
			}
		}
		combo.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				for (IProject project : ResourcesPlugin.getWorkspace().getRoot().getProjects()) {
					if (project.isOpen() && project.getName().equals(combo.getText())) {
						// Assume that users dont have two active projects with the same name.
						if (!project.equals(selectedProject)) {
							// project was changed
							onActiveProjectSwitch(project);
						}
					}
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// No text field in this combo - so this method shouldn't do anything.
			}
		});
	}

	private void onActiveProjectSwitch(IProject project) {
		System.out.println("Switching active project to: " + project.getName());
		selectedProject = project;

		// Ask for configuration data
		configCache = new ConfigCache(project);
		if (configCache.isConfigured() == false) {
			// Ensure the workflow tool is configured first

			ConfigurationDialog configDialog = new ConfigurationDialog(shell, configCache);
			configDialog.create();
			if (configDialog.open() != Window.OK) { // Open dialog and block until it is closed again
				MessageDialog.openError(shell, "Error", "You MUST configure the backend to use the workflow tool");
				return;
			}
		}

		// Initialize APIs
		jiraApi = new WebApi(configCache);
		gitInterface = new GitInterface(project);

		String currentBranch = gitInterface.getCurrentBranch();

		// Update the branch, in case there have been changes in remote
		gitInterface.updateFromRemote(currentBranch);

		// Detect if that project is on the "master" or a issue-branch and open
		// corresponding page

		if (currentBranch.equals("master")) {
			System.out.println("On >master<. Opening MasterPage.");
			showMasterPage();
		} else if (currentBranch.startsWith("issue/")) {
			System.out.println("On >" + currentBranch + "<. Opening WorkingOnIssuePage.");
			try {
				String issueId = currentBranch.replace("issue/", "");
				Issue issue = jiraApi.getIssue(issueId);
				showWorkingOnIssuePage(issue);
			} catch (IOException e) {
				MessageDialog.openError(shell, "Failed to determine active Issue branch", e.getLocalizedMessage());
			}
		} else {
			MessageDialog.openError(shell, "Unexpected git branch",
					"The repository is neither on master, nor a issue branch. Please commit your changes and 'git checkout master' manually.");
		}
	}

}
