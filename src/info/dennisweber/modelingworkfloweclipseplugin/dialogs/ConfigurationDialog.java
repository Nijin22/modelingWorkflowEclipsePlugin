package info.dennisweber.modelingworkfloweclipseplugin.dialogs;

import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import info.dennisweber.modelingworkfloweclipseplugin.ConfigCache;

public class ConfigurationDialog extends TitleAreaDialog {
	private Text repoApiTextfield;
	private Text jiraApiTextfield;
	private Text usernameTextfield;
	private Text passwordTextfield;
	private Shell parentShell;
	private ConfigCache configCache;

	public ConfigurationDialog(Shell parentShell, ConfigCache configCache) {
		super(parentShell);
		this.parentShell = parentShell;
		this.configCache = configCache;
	}

	@Override
	public void create() {
		super.create();
		setTitle("Configure the modelling workflow");
		setMessage("Please configure the URLs to the repository and issue board.", IMessageProvider.NONE);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		GridLayout layout = new GridLayout(2, false);
		container.setLayout(layout);

		// Init all fields
		initRepoApiInput(container);
		initJiraApiInput(container);
		initUsernameInput(container);
		initPasswordInput(container);

		passwordTextfield.setFocus();
		
		return area;
	}

	@Override
	protected void okPressed() {
		configCache.update(repoApiTextfield.getText(), jiraApiTextfield.getText(), usernameTextfield.getText(),
				passwordTextfield.getText());
		MessageDialog.openInformation(parentShell, "Information",
				"In the prototype, changes are only stored in cache and might be lost on reboot of the ACTICO Modeler.");
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

	private void initRepoApiInput(Composite container) {
		Label label = new Label(container, SWT.NONE);
		label.setText("URL of Repository API");

		// Layout:
		GridData gd = new GridData();
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalAlignment = GridData.FILL;
		repoApiTextfield = new Text(container, SWT.BORDER);
		repoApiTextfield.setLayoutData(gd);
		repoApiTextfield.setText(configCache.getRepoApiUrl());

	}

	private void initJiraApiInput(Composite container) {
		Label label = new Label(container, SWT.NONE);
		label.setText("URL of Jira Board API");

		// Layout:
		GridData gd = new GridData();
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalAlignment = GridData.FILL;
		jiraApiTextfield = new Text(container, SWT.BORDER);
		jiraApiTextfield.setLayoutData(gd);
		jiraApiTextfield.setText(configCache.getJiraApiUrl());
	}

	private void initUsernameInput(Composite container) {
		Label label = new Label(container, SWT.NONE);
		label.setText("Username");

		// Layout:
		GridData gd = new GridData();
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalAlignment = GridData.FILL;
		usernameTextfield = new Text(container, SWT.BORDER);
		usernameTextfield.setLayoutData(gd);
		usernameTextfield.setText(configCache.getUsername());
	}

	private void initPasswordInput(Composite container) {
		Label label = new Label(container, SWT.NONE);
		label.setText("Password");

		// Layout:
		GridData gd = new GridData();
		gd.grabExcessHorizontalSpace = true;
		gd.horizontalAlignment = GridData.FILL;
		passwordTextfield = new Text(container, SWT.BORDER | SWT.PASSWORD);
		passwordTextfield.setLayoutData(gd);
		passwordTextfield.setText(configCache.getPassword());
	}
}
