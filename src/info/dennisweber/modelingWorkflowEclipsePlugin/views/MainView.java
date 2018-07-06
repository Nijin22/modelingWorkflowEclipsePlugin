package info.dennisweber.modelingworkfloweclipseplugin.views;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.part.ViewPart;

import info.dennisweber.modelingworkfloweclipseplugin.ConfigCache;
import info.dennisweber.modelingworkfloweclipseplugin.dialogs.ConfigurationDialog;
import info.dennisweber.modelingworkfloweclipseplugin.model.JiraRestApi;

public class MainView extends ViewPart {
	private ConfigCache configCache;
	private JiraRestApi jiraApi;
	private IProject selectedProject = null;
	private MasterPage startseite;
	Shell shell;

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

		initProjectSelectionCombo(parent);

		if (selectedProject != null) {

		}

	}

	private void initProjectSelectionCombo(Composite parent) {
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
							System.out.println("Switching active project to: " + project.getName());
							selectedProject = project;

							// Close old data, if it is there
							if (startseite != null) {
								startseite.dispose();
								startseite = null;
							}

							configCache = new ConfigCache();
							if (configCache.isConfigured() == false) {
								// Ensure the workflow tool is configured first

								ConfigurationDialog configDialog = new ConfigurationDialog(shell, configCache);
								configDialog.create();
								configDialog.open(); // Open dialog and block until it is closed again
							}

							jiraApi = new JiraRestApi(configCache);

							startseite = new MasterPage(parent, jiraApi, configCache, shell);
							startseite.init();
							shell.redraw();
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

}
